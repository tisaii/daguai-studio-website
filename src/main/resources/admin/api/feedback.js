// 查询列表页接口
const getFeedbackDetailPage = (params) => {
  return $axios({
    url: '/feedback/page',
    method: 'get',
    params
  })
}

// 查看接口
const queryFeedbackDetailById = (id) => {
  return $axios({
    url: `/feedbackDetail/${id}`,
    method: 'get'
  })
}

// 更新回复
const replyFeedback =(params) => {
  return $axios({
    url: '/feedback/reply',
    method: 'put',
    data: {...params}
  })
}

// 删除数据接口
const deleteFeedback = (ids) => {
  return $axios({
    url: '/feedback',
    method: 'delete',
    params: { ids }
  })
}

//添加反馈接口
const addFeedback =(params) => {
  return $axios({
    url: '/feedback',
    method: 'post',
    data: params
  })
}

// // 取消，派送，完成接口
// const editOrderDetail = (params) => {
//   return $axios({
//     url: '/order',
//     method: 'put',
//     data: { ...params }
//   })
// }
