/**
 * 社保公积金模块前端 API
 * 包含：城市字典、险种字典、行业字典、社保参数、公积金参数、核算明细、年度基数重算
 */
import { get, post, request } from '@/utils/request'

// ========== 城市字典 ==========
export const getCityPageApi = (params: any) => get('/hr/city/page', params)
export const addCityApi = (data: any) => post('/hr/city', data)
export const updateCityApi = (data: any) => request({ url: '/hr/city', method: 'put', data })
export const deleteCityApi = (id: number) => request({ url: `/hr/city/${id}`, method: 'delete' })

// ========== 险种字典 ==========
export const getInsuranceTypePageApi = (params: any) => get('/hr/insurance-type/page', params)
export const addInsuranceTypeApi = (data: any) => post('/hr/insurance-type', data)
export const updateInsuranceTypeApi = (data: any) => request({ url: '/hr/insurance-type', method: 'put', data })
export const deleteInsuranceTypeApi = (id: number) => request({ url: `/hr/insurance-type/${id}`, method: 'delete' })

// ========== 行业字典 ==========
export const getIndustryPageApi = (params: any) => get('/hr/industry/page', params)
export const addIndustryApi = (data: any) => post('/hr/industry', data)
export const updateIndustryApi = (data: any) => request({ url: '/hr/industry', method: 'put', data })
export const deleteIndustryApi = (id: number) => request({ url: `/hr/industry/${id}`, method: 'delete' })

// ========== 社保参数配置 ==========
export const getSocialParamPageApi = (params: any) => get('/hr/social-param/page', params)
export const addSocialParamApi = (data: any) => post('/hr/social-param', data)
export const updateSocialParamApi = (data: any) => request({ url: '/hr/social-param', method: 'put', data })
export const activateSocialParamApi = (id: number) => post(`/hr/social-param/${id}/activate`)
export const toggleSocialParamApi = (id: number) => post(`/hr/social-param/${id}/toggle`)
export const deleteSocialParamApi = (id: number) => request({ url: `/hr/social-param/${id}`, method: 'delete' })

// ========== 公积金参数配置 ==========
export const getHousingFundPageApi = (params: any) => get('/hr/housing-fund/page', params)
export const addHousingFundApi = (data: any) => post('/hr/housing-fund', data)
export const activateHousingFundApi = (id: number) => post(`/hr/housing-fund/${id}/activate`)
export const deleteHousingFundApi = (id: number) => request({ url: `/hr/housing-fund/${id}`, method: 'delete' })

// ========== 社保核算明细 ==========
export const getCalcDetailPageApi = (params: any) => get('/hr/social-calc/detail/page', params)
export const exportCalcDetailApi = (params: any) => get('/hr/social-calc/detail/export', params)

// ========== 年度基数重算 ==========
export const triggerAnnualRecalcApi = (data: any) => post('/hr/social-calc/annual-recalc', data)
