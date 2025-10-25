import { axiosApi } from "@/api/backend-api";
import { AxiosResponse } from "axios";

const userApi = {
    async register(userData: { username: string; email: string; password: string; }) {
        return await axiosApi.post(`/auth/register`, userData)
    },

    async login(credentials: { username: string; password: string; rememberMe: boolean }) {
        return await axiosApi.post(`/auth/login`, credentials)
    },

    async getLoggedInUser() {
        return await axiosApi.get(`/auth/me`)
    },

    async logout() {
        return await axiosApi.post(`/auth/logout`)
    },

    async forgotPassword(email: string) {
        return await axiosApi.post(`/auth/forgot-password`, { email })
    },

    async resetPassword(token: string, newPassword: string) {
        return await axiosApi.post(`/auth/reset-password`, { token, newPassword })
    }
}

export default userApi
