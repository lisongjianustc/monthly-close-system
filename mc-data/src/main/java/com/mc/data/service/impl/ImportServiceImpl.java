package com.mc.data.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.common.enums.BatchStatus;
import com.mc.common.enums.RecordStatus;
import com.mc.common.exception.BusinessException;
import com.mc.common.result.ResultCode;
import com.mc.data.dto.HeaderMapping;
import com.mc.data.dto.ImportProgress;
import com.mc.data.entity.ImportBatch;
import com.mc.data.entity.ImportRecord;
import com.mc.data.entity.ImportTemplate;
import com.mc.data.mapper.ImportBatchMapper;
import com.mc.data.mapper.ImportRecordMapper;
import com.mc.data.mapper.ImportTemplateMapper;
import com.mc.data.service.IImportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Collections.emptyList;

/**
 * 数据导入Service实现
 */
@Service
public class ImportServiceImpl extends ServiceImpl<ImportBatchMapper, ImportBatch> implements IImportService {

    private static final int BATCH_SIZE = 1000;

    @Autowired
    private ImportRecordMapper recordMapper;

    @Autowired
    private ImportTemplateMapper templateMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public Long createBatch(String importType, String fileName, String filePath, Long fileSize, Long userId, String userName) {
        ImportBatch batch = new ImportBatch();
        batch.setBatchNo(generateBatchNo());
        batch.setImportType(importType);
        batch.setFileName(fileName);
        batch.setFilePath(filePath);
        batch.setFileSize(fileSize);
        batch.setImportUserId(userId);
        batch.setImportUserName(userName);
        batch.setStatus(BatchStatus.PENDING.name());
        batch.setStartTime(LocalDateTime.now());
        this.save(batch);
        return batch.getId();
    }

    /**
     * 执行导入：文件解析在事务外，批量入库在事务内
     */
    public void executeImport(Long batchId, String templateCode) {
        ImportBatch batch = this.getById(batchId);
        if (batch == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "导入批次不存在");
        }
        if (!BatchStatus.PENDING.name().equals(batch.getStatus())) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "批次状态不允许执行导入");
        }

        batch.setStatus(BatchStatus.PROCESSING.name());
        this.updateById(batch);

        // 解析文件在事务外，避免长事务锁住数据库连接
        List<ImportRecord> records;
        try {
            ImportTemplate template = null;
            if (templateCode != null && !templateCode.isEmpty()) {
                template = templateMapper.selectList(null).stream()
                    .filter(t -> templateCode.equals(t.getCode()))
                    .findFirst()
                    .orElse(null);
            }
            records = parseFile(batch, template);
        } catch (Exception e) {
            batch.setStatus(BatchStatus.FAILED.name());
            batch.setErrorMsg(e.getMessage());
            batch.setEndTime(LocalDateTime.now());
            this.updateById(batch);
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "文件解析失败: " + e.getMessage());
        }

        // 批量入库（事务内）
        doInsertBatch(batchId, records);
    }

    /**
     * 批量插入记录（事务内）
     */
    @Transactional
    public void doInsertBatch(Long batchId, List<ImportRecord> records) {
        ImportBatch batch = this.getById(batchId);
        if (batch == null) return;

        List<ImportRecord> toInsert = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        for (ImportRecord record : records) {
            record.setBatchId(batchId);
            record.setStatus(RecordStatus.PENDING.name());
            toInsert.add(record);

            // 分批 flush
            if (toInsert.size() >= BATCH_SIZE) {
                flushBatch(toInsert);
                successCount += toInsert.size();
                toInsert.clear();
            }
        }

        // 处理剩余记录
        if (!toInsert.isEmpty()) {
            flushBatch(toInsert);
            successCount += toInsert.size();
        }

        batch.setTotalRecords(records.size());
        batch.setSuccessRecords(successCount);
        batch.setFailRecords(failCount);
        batch.setStatus(failCount > 0 ? BatchStatus.PARTIAL_SUCCESS.name() : BatchStatus.COMPLETED.name());
        batch.setEndTime(LocalDateTime.now());
        this.updateById(batch);
    }

    private void flushBatch(List<ImportRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        try {
            // 尝试批量插入
            recordMapper.insertBatchForMySQL(records);
        } catch (Exception e) {
            // 批量插入失败，降级为逐条插入（失败记录标记INVALID）
            for (ImportRecord record : records) {
                try {
                    recordMapper.insert(record);
                } catch (Exception inner) {
                    record.setStatus(RecordStatus.INVALID.name());
                    record.setErrorMsg("数据库写入失败: " + inner.getMessage());
                    try {
                        recordMapper.insert(record);
                    } catch (Exception ignored) {
                        // 二次失败无法挽救，跳过
                    }
                }
            }
        }
    }

    @Override
    public ImportProgress getImportProgress(Long batchId) {
        ImportBatch batch = this.getById(batchId);
        if (batch == null) {
            return null;
        }

        ImportProgress progress = new ImportProgress();
        progress.setBatchId(batchId);
        progress.setBatchNo(batch.getBatchNo());
        progress.setStatus(batch.getStatus());

        Integer total = batch.getTotalRecords();
        Integer success = batch.getSuccessRecords();
        Integer fail = batch.getFailRecords();

        int safeTotal = (total != null ? total : 0);
        int safeSuccess = (success != null ? success : 0);
        int safeFail = (fail != null ? fail : 0);

        progress.setTotalRecords(safeTotal);
        progress.setSuccessRecords(safeSuccess);
        progress.setFailRecords(safeFail);

        int processed = safeSuccess + safeFail;
        progress.setProcessedRecords(processed);

        if (safeTotal > 0) {
            progress.setProgressPercent((processed * 100) / safeTotal);
        } else {
            progress.setProgressPercent(0);
        }

        return progress;
    }

    @Override
    public List<ImportRecord> getBatchRecords(Long batchId) {
        return recordMapper.selectByBatchId(batchId);
    }

    @Override
    public List<ImportRecord> getFailRecords(Long batchId) {
        return recordMapper.selectFailRecords(batchId);
    }

    @Override
    @Transactional
    public void validateData(Long batchId) {
        List<ImportRecord> records = recordMapper.selectByBatchId(batchId);
        if (records == null || records.isEmpty()) {
            return;
        }

        for (ImportRecord record : records) {
            boolean invalid = false;
            String errorMsg = null;

            if (record.getAccountCode() == null || record.getAccountCode().isEmpty()) {
                invalid = true;
                errorMsg = "科目编码不能为空";
            } else if (record.getAmount() == null) {
                invalid = true;
                errorMsg = "金额不能为空";
            }

            if (invalid) {
                record.setStatus(RecordStatus.INVALID.name());
                record.setErrorMsg(errorMsg);
            } else {
                record.setStatus(RecordStatus.VALID.name());
            }
            record.setValidateTime(LocalDateTime.now());
        }

        // 批量更新：分批调用mapper中的批量更新方法
        int batchSize = 200;
        for (int i = 0; i < records.size(); i += batchSize) {
            int end = Math.min(i + batchSize, records.size());
            List<ImportRecord> batch = records.subList(i, end);
            recordMapper.updateStatusBatch(batch);
        }
    }

    @Override
    @Transactional
    public boolean cancelImport(Long batchId) {
        ImportBatch batch = this.getById(batchId);
        if (batch == null) {
            return false;
        }
        if (BatchStatus.PROCESSING.name().equals(batch.getStatus())) {
            return false;
        }
        batch.setStatus(BatchStatus.CANCELLED.name());
        batch.setEndTime(LocalDateTime.now());
        return this.updateById(batch);
    }

    private List<ImportRecord> parseFile(ImportBatch batch, ImportTemplate template) throws Exception {
        List<ImportRecord> records;

        if ("EXCEL".equals(batch.getImportType())) {
            records = parseExcel(batch.getFilePath(), template);
        } else if ("CSV".equals(batch.getImportType())) {
            records = parseCsv(batch.getFilePath(), template);
        } else {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "不支持的导入类型: " + batch.getImportType());
        }

        return records;
    }

    private List<ImportRecord> parseExcel(String filePath, ImportTemplate template) throws Exception {
        List<ImportRecord> records = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(new java.io.File(filePath))) {
            Sheet sheet = workbook.getSheetAt(0);
            int dataStartRow = 1;

            if (template != null && template.getDataStartRow() != null) {
                dataStartRow = template.getDataStartRow();
            }

            Map<String, Integer> headerMap = new HashMap<>();
            Row headerRow = sheet.getRow(dataStartRow - 1);
            if (headerRow != null) {
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell cell = headerRow.getCell(i);
                    if (cell != null) {
                        headerMap.put(getCellStringValue(cell), i);
                    }
                }
            }

            for (int i = dataStartRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                ImportRecord record = new ImportRecord();
                record.setRowNo(i + 1);
                record.setStatus(RecordStatus.PENDING.name());

                if (template != null && template.getHeaderMappings() != null) {
                    try {
                        List<HeaderMapping> mappings = objectMapper.readValue(
                            template.getHeaderMappings(),
                            objectMapper.getTypeFactory().constructCollectionType(List.class, HeaderMapping.class)
                        );
                        for (HeaderMapping mapping : mappings) {
                            Integer colIndex = headerMap.get(mapping.getExcelHeader());
                            if (colIndex != null) {
                                Cell cell = row.getCell(colIndex);
                                String value = cell != null ? getCellStringValue(cell) : null;
                                setRecordField(record, mapping.getFieldCode(), value);
                            }
                        }
                    } catch (Exception e) {
                        // 使用默认解析
                        record.setAccountCode(getCellStringValue(row.getCell(0)));
                        record.setAccountName(getCellStringValue(row.getCell(1)));
                        String amountStr = getCellStringValue(row.getCell(2));
                        if (amountStr != null && !amountStr.isEmpty()) {
                            record.setAmount(new BigDecimal(amountStr));
                        }
                    }
                } else {
                    // 默认解析前几列
                    record.setAccountCode(getCellStringValue(row.getCell(0)));
                    record.setAccountName(getCellStringValue(row.getCell(1)));
                    String amountStr = getCellStringValue(row.getCell(2));
                    if (amountStr != null && !amountStr.isEmpty()) {
                        record.setAmount(new BigDecimal(amountStr));
                    }
                }

                records.add(record);
            }
        }

        return records;
    }

    private List<ImportRecord> parseCsv(String filePath, ImportTemplate template) throws Exception {
        List<ImportRecord> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new java.io.FileInputStream(filePath), "UTF-8"))) {
            String line;
            int rowNo = 0;
            int dataStartRow = 1;

            if (template != null && template.getDataStartRow() != null) {
                dataStartRow = template.getDataStartRow();
            }

            while ((line = reader.readLine()) != null) {
                rowNo++;
                if (rowNo < dataStartRow) {
                    continue;
                }
                if (line.trim().isEmpty()) {
                    continue;
                }

                // CSV注入防护：忽略以 =,+,-,@ 开头的单元格
                String[] fields = line.split(",", -1);
                ImportRecord record = new ImportRecord();
                record.setRowNo(rowNo);
                record.setStatus(RecordStatus.PENDING.name());

                if (fields.length > 0) record.setAccountCode(sanitizeField(fields[0].trim()));
                if (fields.length > 1) record.setAccountName(sanitizeField(fields[1].trim()));
                if (fields.length > 2) {
                    try {
                        record.setAmount(new BigDecimal(fields[2].trim()));
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
                if (fields.length > 3) record.setDimension1(sanitizeField(fields[3].trim()));
                if (fields.length > 4) record.setDimension2(sanitizeField(fields[4].trim()));
                if (fields.length > 5) record.setDimension3(sanitizeField(fields[5].trim()));

                records.add(record);
            }
        }

        return records;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellStringValue(cell);
                if (value != null && !value.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void setRecordField(ImportRecord record, String fieldCode, String value) {
        if (fieldCode == null || value == null) {
            return;
        }
        switch (fieldCode) {
            case "accountCode" -> record.setAccountCode(value);
            case "accountName" -> record.setAccountName(value);
            case "amount" -> {
                try {
                    record.setAmount(new BigDecimal(value));
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
            case "dimension1" -> record.setDimension1(value);
            case "dimension2" -> record.setDimension2(value);
            case "dimension3" -> record.setDimension3(value);
        }
    }

    /**
     * CSV注入防护：若字段以 =,+,-,@ 开头则前缀单引号防止公式执行
     */
    private String sanitizeField(String value) {
        if (value != null && !value.isEmpty()
            && (value.startsWith("=") || value.startsWith("+")
                || value.startsWith("-") || value.startsWith("@"))) {
            return "'" + value;
        }
        return value;
    }

    private String generateBatchNo() {
        return "IMP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            + String.format("%04d", new Random().nextInt(10000));
    }
}
