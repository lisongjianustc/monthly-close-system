// Simplified E2E Test - quick validation of all pages
const { chromium } = require('@playwright/test');

const BASE_URL = 'http://localhost:3000';
const results = [];

async function log(section, item, pass, note = '') {
  const status = pass ? '✅ PASS' : '❌ FAIL';
  results.push({ section, item, pass, note });
  console.log(`${status} | ${section} | ${item}${note ? ' | ' + note : ''}`);
}

async function runTests() {
  console.log('🚀 启动浏览器...\n');
  
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  
  page.on('pageerror', err => console.log(`  [Error] ${err.message.substring(0, 80)}`));
  
  try {
    // Login
    console.log('=== 登录测试 ===');
    await page.goto(`${BASE_URL}/login`, { timeout: 10000 });
    await page.waitForTimeout(2000);
    
    const title = await page.title();
    log('F01', '页面标题', title.includes('月结') || title.includes('login'), `title=${title}`);
    
    const inputs = await page.locator('input').count();
    log('F01', '输入框存在', inputs >= 2, `inputs=${inputs}`);
    
    const btn = await page.locator('button').first();
    log('F01', '登录按钮存在', await btn.isVisible());
    
    // Try to fill login form
    const usernameInput = page.locator('input').nth(0);
    const passwordInput = page.locator('input').nth(1);
    
    await usernameInput.fill('admin', { timeout: 5000 });
    await passwordInput.fill('admin123', { timeout: 5000 });
    await btn.click();
    await page.waitForTimeout(3000);
    
    const afterLoginUrl = page.url();
    const loginSuccess = !afterLoginUrl.includes('/login');
    log('F01', '登录成功', loginSuccess, `url=${afterLoginUrl}`);
    
    if (loginSuccess) {
      console.log('\n=== 各模块页面测试 ===');
      
      const modules = [
        { path: '/system/org', name: 'F02', label: '组织管理' },
        { path: '/system/user', name: 'F03', label: '用户管理' },
        { path: '/system/role', name: 'F04', label: '角色管理' },
        { path: '/system/period', name: 'F05', label: '期间管理' },
        { path: '/workflow/template', name: 'F06', label: '流程模板' },
        { path: '/workflow/instance', name: 'F07', label: '流程实例' },
        { path: '/workflow/task', name: 'F08', label: '任务管理' },
        { path: '/data/import', name: 'F09', label: '数据导入' },
        { path: '/rule', name: 'F10', label: '规则配置' },
        { path: '/exception', name: 'F11', label: '异常处理' },
        { path: '/notification', name: 'F12', label: '通知管理' },
      ];
      
      for (const mod of modules) {
        try {
          await page.goto(`${BASE_URL}${mod.path}`, { timeout: 8000 });
          await page.waitForTimeout(1500);
          
          const url = page.url();
          log(mod.name, `${mod.label}页面加载`, url.includes(mod.path.split('/')[1]) || url.includes(mod.path), `url=${url}`);
          
          // Check for table or main content
          const hasContent = await page.locator('.el-table, .el-card, .el-tabs, main, [class*="main"]').first().isVisible().catch(() => false);
          log(mod.name, `${mod.label}内容区域`, hasContent);
          
        } catch (e) {
          log(mod.name, `${mod.label}`, false, `Error: ${e.message.substring(0, 50)}`);
        }
      }
      
      // Dashboard check
      console.log('\n=== Dashboard测试 ===');
      await page.goto(`${BASE_URL}/`, { timeout: 8000 });
      await page.waitForTimeout(2000);
      const cards = await page.locator('.el-card').count();
      log('F01', 'Dashboard统计卡片', cards >= 2, `cards=${cards}`);
    }
    
    // Route guard test
    console.log('\n=== 路由守卫测试 ===');
    await page.context().clearCookies();
    await page.goto(`${BASE_URL}/system/user`, { timeout: 8000 });
    await page.waitForTimeout(1500);
    const guardUrl = page.url();
    log('F13', '未登录访问跳转/login', guardUrl.includes('login'), `url=${guardUrl}`);
    
  } catch (error) {
    console.error('\n❌ 测试异常:', error.message);
  } finally {
    await browser.close();
  }
  
  // Summary
  console.log('\n\n========================================');
  console.log('          E2E测试结果汇总');
  console.log('========================================');
  
  const sections = {};
  results.forEach(r => {
    if (!sections[r.section]) sections[r.section] = { pass: 0, fail: 0 };
    r.pass ? sections[r.section].pass++ : sections[r.section].fail++;
  });
  
  let totalPass = 0, totalFail = 0;
  Object.keys(sections).sort().forEach(section => {
    const s = sections[section];
    const total = s.pass + s.fail;
    const rate = total > 0 ? Math.round(s.pass / total * 100) : 0;
    console.log(`  ${section.padEnd(6)} | ${String(s.pass).padStart(2)}/${String(total).padStart(2)} | 通过率 ${rate}%`);
    totalPass += s.pass;
    totalFail += s.fail;
  });
  
  const allTotal = totalPass + totalFail;
  const allRate = allTotal > 0 ? Math.round(totalPass / allTotal * 100) : 0;
  console.log('----------------------------------------');
  console.log(`  总计   | ${String(totalPass).padStart(2)}/${String(allTotal).padStart(2)} | 通过率 ${allRate}%`);
  console.log('========================================\n');
  
  const fs = require('fs');
  fs.writeFileSync('/Users/lisongjian/Project/projectX/monthly-close-system/docs/test/E2E_TEST_RESULT.json', 
    JSON.stringify({ timestamp: new Date().toISOString(), total: allTotal, passed: totalPass, failed: totalFail, rate: allRate, results }, null, 2));
  console.log('📄 结果已保存');
}

runTests().catch(console.error);
