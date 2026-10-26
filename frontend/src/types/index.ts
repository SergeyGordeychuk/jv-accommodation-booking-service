export type AccommodationType = 'HOUSE' | 'APARTMENT' | 'CONDO' | 'VACATION_HOME';

export interface Accommodation {
  id: number;
  type: AccommodationType;
  location: string;
  size: string;
  amenities: string | null;
  dailyRate: number;
  availability: number;
}

export interface AccommodationRequest {
  type: AccommodationType;
  location: string;
  size: string;
  amenities?: string;
  dailyRate: number;
  availability: number;
}

export interface AccommodationPatchRequest {
  type?: AccommodationType;
  location?: string;
  size?: string;
  amenities?: string;
  dailyRate?: number;
  availability?: number;
}

export type UserRole = 'CUSTOMER' | 'MANAGER';

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
}

export interface UserLoginRequest {
  email: string;
  password: string;
}

export interface UserLoginResponse {
  token: string;
}

export interface UserRegistrationRequest {
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  repeatPassword: string;
}

export interface ApiError {
  status: number;
  error?: string;
  message?: string;
  errors?: Record<string, string>;
}
