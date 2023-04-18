// 查询列表数据
const getBlogPage = (params) => {
  return $axios({
    url: '/blog/page',
    method: 'get',
    params
  })
}

// 删除数据接口
const deleteBlog = (ids) => {
  return $axios({
    url: '/blog',
    method: 'delete',
    params: { ids }
  })
}

// 修改数据接口
const editBlog = (params) => {
  return $axios({
    url: '/blog',
    method: 'put',
    data: { ...params }
  })
}

// 新增数据接口
const addBlog = (params) => {
  return $axios({
    url: '/blog',
    method: 'post',
    data: { ...params }
  })
}

// 查询详情接口
const queryBlogById = (id) => {
  return $axios({
    url: `/blog/${id}`,
    method: 'get'
  })
}

