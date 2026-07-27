export interface ClientProfile {
  id: number;
  name: string;
  email: string;
  phone: string | null;
  photoUrl: string | null;
  updatedAt?: string;
}

export interface ProfileUpdateRequest {
  name: string;
  phone: string | null;
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
}

export interface PhotoUploadResponse {
  photoUrl: string;
}
