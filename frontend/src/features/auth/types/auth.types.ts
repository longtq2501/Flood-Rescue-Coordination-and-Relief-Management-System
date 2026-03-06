import { ROLES } from "../../../constants/roles";


export type Role = keyof typeof ROLES;

export interface User {
    id: number;
    fullName: string;
    phone: string;
    email: string;
    role: Role;
    status: 'ACTIVE' | 'INACTIVE' | 'BANNED';
    avatarUrl?: string;
}

export interface LoginRequest {
    phone: string;
    password: string;
}

export interface RegisterRequest {
    fullName: string;
    phone: string;
    email: string;
    password: string;
    role: 'CITIZEN' | 'RESCUE_TEAM';
}

export interface LoginResponse {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresIn: number;
    user: User;
}