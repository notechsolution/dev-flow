import { defineStore } from "pinia";

import api from '../api/backend-api'

export const userStore = defineStore("user", {
    state: () => ({
        id: null as string | null,
        username: null as string | null,
        email: null as string | null,
        role: null as string | null,
        projectIds: [] as string[]
    }),
    actions: {
        login(id: string, username: string, email: string, role: string, projectIds: string[]): any {
            this.id = id;
            this.username = username;
            this.email = email;
            this.role = role;
            this.projectIds = projectIds || [];
        },
        updateProfile(email: string): void {
            this.email = email;
        },
        logout(): any {
            return new Promise((resolve, reject) => {
                api.logout()
                        .then(response => {
                            this.id = null;
                            this.username = null;
                            this.email = null;
                            this.role = null;
                            this.projectIds = [];
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
        getUsername: state => state.username,
        getUserId: state => state.id,
        getEmail: state => state.email
    }
})
