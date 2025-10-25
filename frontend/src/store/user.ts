import { defineStore } from "pinia";

import api from '../api/backend-api'

export const userStore = defineStore("user", {
    state: () => ({
        username: null,
        role: null,
        projectIds: [] as string[]
    }),
    actions: {
        login(username: any, role: any, projectIds: string[]): any {
            this.username = username;
            this.role = role;
            this.projectIds = projectIds;
        },
        logout(): any {
            return new Promise((resolve, reject) => {
                api.logout()
                        .then(response => {
                            this.username = null;
                            resolve(response);
                        })
                        .catch((error) => {
                            console.log("Error: " + error);
                            reject(error);
                        });
            });
        }
    },
    getters: {
        getUsername: state => state.username
    }
})
