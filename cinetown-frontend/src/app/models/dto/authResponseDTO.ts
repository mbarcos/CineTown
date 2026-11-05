export interface AuthResponseDTO {
  token: string;
  tokenType: string;
  nombreUsuario: string;
  roles: string[];
}
