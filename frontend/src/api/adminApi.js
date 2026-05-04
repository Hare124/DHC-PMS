// src/api/adminApi.js
import request from '@/utils/request'

/**
 * 物业人员信息管理接口（基于User实体，role=1）
 */

// 1. 分页查询物业人员列表
export function getStaffList(params) {
    return request({
        url: '/admin/property-staff',
        method: 'get',
        params // 携带pageNum/pageSize/keyword参数
    })
}

// 2. 新增物业人员
export function addStaff(data) {
    return request({
        url: '/admin/property-staff',
        method: 'post',
        data // 携带username/name/phone/password等User实体字段
    })
}

// 3. 修改物业人员信息
export function updateStaff(data) {
    return request({
        url: `/admin/property-staff/${data.id}`,
        method: 'put',
        data // 携带修改后的User实体字段（id必传）
    })
}

// 4. 删除物业人员
export function deleteStaff(id) {
    return request({
        url: `/admin/property-staff/${id}`,
        method: 'delete'
    })
}


// 5. 修改密码
export function changePassword(data) {
    return request({
        url: '/admin/change-password',
        method: 'put',
        params: data // 携带 oldPassword/newPassword 参数
    })
}

// 6. 获取首页概览统计数据
export function getOverviewData() {
    return request({
        url: '/admin/overview',
        method: 'get'
    })
}

// 7. 更新用户资料
export function updateUserProfile(data) {
    return request({
        url: '/user',
        method: 'put',
        data // 携带 id/name/phone 等字段
    })
}

// ====================== 设备管理接口 ======================

// 8. 分页查询设备列表
export function getEquipmentList(params) {
    return request({
        url: '/admin/equipment/list',
        method: 'get',
        params // 携带pageNum/pageSize/keyword/type/status 参数
    })
}

// 9. 根据 ID 查询设备详情
export function getEquipmentById(id) {
    return request({
        url: `/admin/equipment/${id}`,
        method: 'get'
    })
}

// 10. 新增设备
export function addEquipment(data) {
    return request({
        url: '/admin/equipment',
        method: 'post',
        data
    })
}

// 11. 修改设备信息
export function updateEquipment(id, data) {
    return request({
        url: `/admin/equipment/${id}`,
        method: 'put',
        data
    })
}

// 12. 删除设备
export function deleteEquipment(id) {
    return request({
        url: `/admin/equipment/${id}`,
        method: 'delete'
    })
}

// 13. 更新设备状态
export function updateEquipmentStatus(id, status) {
    return request({
        url: `/admin/equipment/${id}/status`,
        method: 'put',
        params: { status }
    })
}

// ====================== 制度管理接口 ======================

// 14. 分页查询制度列表
export function getRegulationList(params) {
    return request({
        url: '/admin/regulation/list',
        method: 'get',
        params // 携带 pageNum/pageSize/keyword/type/status 参数
    })
}

// 15. 根据 ID 查询制度详情
export function getRegulationById(id) {
    return request({
        url: `/admin/regulation/${id}`,
        method: 'get'
    })
}

// 16. 新增制度
export function addRegulation(data) {
    return request({
        url: '/admin/regulation',
        method: 'post',
        params: {
            createBy: data.createBy,
            createByName: data.createByName
        },
        data
    })
}

// 17. 修改制度信息
export function updateRegulation(id, data) {
    return request({
        url: `/admin/regulation/${id}`,
        method: 'put',
        params: {
            updateBy: data.updateBy,
            updateByName: data.updateByName
        },
        data
    })
}

// 18. 删除制度
export function deleteRegulation(id) {
    return request({
        url: `/admin/regulation/${id}`,
        method: 'delete'
    })
}

// 19. 创建新版本制度
export function createNewVersion(id, data) {
    return request({
        url: `/admin/regulation/${id}/newVersion`,
        method: 'post',
        params: {
            createBy: data.createBy,
            createByName: data.createByName
        },
        data
    })
}

// 20. 发布制度
export function publishRegulation(id) {
    return request({
        url: `/admin/regulation/${id}/publish`,
        method: 'put'
    })
}

// 21. 下架制度
export function suspendRegulation(id) {
    return request({
        url: `/admin/regulation/${id}/suspend`,
        method: 'put'
    })
}

// 22. 作废制度
export function invalidateRegulation(id) {
    return request({
        url: `/admin/regulation/${id}/invalidate`,
        method: 'put'
    })
}

// ====================== 轮播图管理接口 ======================

// 23. 分页查询轮播图列表
export function getCarouselList(params) {
    return request({
        url: '/admin/carousel/list',
        method: 'get',
        params // 携带 pageNum/pageSize/keyword/status 参数
    })
}

// 24. 查询启用的轮播图（前端首页使用）
export function getActiveCarousels() {
    return request({
        url: '/admin/carousel/active',
        method: 'get'
    })
}

// 25. 根据 ID 查询轮播图详情
export function getCarouselById(id) {
    return request({
        url: `/admin/carousel/${id}`,
        method: 'get'
    })
}

// 26. 新增轮播图
export function addCarousel(data) {
    return request({
        url: '/admin/carousel',
        method: 'post',
        params: {
            createBy: data.createBy,
            createByName: data.createByName
        },
        data
    })
}

// 27. 修改轮播图信息
export function updateCarousel(id, data) {
    return request({
        url: `/admin/carousel/${id}`,
        method: 'put',
        params: {
            updateBy: data.updateBy,
            updateByName: data.updateByName
        },
        data
    })
}

// 28. 删除轮播图
export function deleteCarousel(id) {
    return request({
        url: `/admin/carousel/${id}`,
        method: 'delete'
    })
}

// 29. 更新轮播图状态
export function updateCarouselStatus(id, status) {
    return request({
        url: `/admin/carousel/${id}/status`,
        method: 'put',
        params: { status }
    })
}

// ====================== 测试接口 ======================
// 测试 POST 接口
export const testPost = (data) => {
    return request({
        url: '/user/test01', // 最终请求路径：/api/user/test
        method: 'post',
        data
    });
};

// 测试 GET 接口
export const testGet = () => {
    return request({
        url: '/user/test02', // 最终请求路径：/api/user/list
        method: 'get'
    });
};