function loginApi(data) {
  return $axios({
    'url': '/account/login',
    'method': 'post',
    data
  })
}

function logoutApi(){
  return $axios({
    'url': '/account/logout',
    'method': 'post',
  })
}
