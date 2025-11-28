// 测试API的JavaScript代码 (在浏览器控制台执行)

// 测试获取提交列表
async function testSubmissionList() {
  try {
    const response = await fetch('/dev-api/system/assignment/submission/list?assignmentId=38', {
      headers: {
        'Authorization': 'Bearer ' + localStorage.getItem('token')
      }
    });
    const data = await response.json();
    console.log('提交列表API响应:', data);
    return data;
  } catch (error) {
    console.error('提交列表API错误:', error);
  }
}

// 测试获取单个提交详情
async function testSubmissionDetail(submissionId) {
  try {
    const response = await fetch(`/dev-api/system/assignment/submission/${submissionId}`, {
      headers: {
        'Authorization': 'Bearer ' + localStorage.getItem('token')
      }
    });
    const data = await response.json();
    console.log('提交详情API响应:', data);
    return data;
  } catch (error) {
    console.error('提交详情API错误:', error);
  }
}

// 运行测试
console.log('开始测试API...');
testSubmissionList().then(listData => {
  if (listData && listData.rows && listData.rows.length > 0) {
    const firstSubmission = listData.rows[0];
    console.log('找到提交记录，ID:', firstSubmission.id);
    return testSubmissionDetail(firstSubmission.id);
  } else {
    console.log('没有找到提交记录，可能需要先插入测试数据');
  }
});