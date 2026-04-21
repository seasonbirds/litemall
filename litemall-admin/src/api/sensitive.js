import request from '@/utils/request'

export function listSensitive(query) {
  return request({
    url: '/sensitive/list',
    method: 'get',
    params: query
  })
}

export function createSensitive(data) {
  return request({
    url: '/sensitive/create',
    method: 'post',
    data
  })
}

export function readSensitive(data) {
  return request({
    url: '/sensitive/read',
    method: 'get',
    data
  })
}

export function updateSensitive(data) {
  return request({
    url: '/sensitive/update',
    method: 'post',
    data
  })
}

export function deleteSensitive(data) {
  return request({
    url: '/sensitive/delete',
    method: 'post',
    data
  })
}

export function enableSensitive(data) {
  return request({
    url: '/sensitive/enable',
    method: 'post',
    data
  })
}

export function disableSensitive(data) {
  return request({
    url: '/sensitive/disable',
    method: 'post',
    data
  })
}
