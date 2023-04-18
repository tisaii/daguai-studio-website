// 查询列表接口
const getProductPage = (params) => {
  return $axios({
    url: '/product/page',
    method: 'get',
    params
  })
}

// 删除接口
const deleteDish = (ids) => {
  return $axios({
    url: '/product',
    method: 'delete',
    params: { ids }
  })
}

// 修改接口
const editDish = (params) => {
  return $axios({
    url: '/product',
    method: 'put',
    data: { ...params }
  })
}

// 新增接口
const addDish = (params) => {
  return $axios({
    url: '/product',
    method: 'post',
    data: { ...params }
  })
}

// 查询详情
const queryProductById = (id) => {
  return $axios({
    url: `/product/${id}`,
    method: 'get'
  })
}

// 获取菜品分类列表
const getCategoryList = (params) => {
  return $axios({
    url: '/category/list',
    method: 'get',
    params
  })
}

// 查菜品列表的接口
const queryProductList = (params) => {
  return $axios({
    url: '/product/list',
    method: 'get',
    params
  })
}

// 文件down预览
const commonDownload = (params) => {
  return $axios({
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'
    },
    url: '/apireq/common/download',
    method: 'get',
    params
  })
}

// 起售停售---批量起售停售接口
// const dishStatusByStatus = (params) => {
//   return $axios({
//     url: `/dish/status/${params.status}`,
//     method: 'post',
//     params: { ids: params.id }
//   })
// }