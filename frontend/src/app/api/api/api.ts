export * from './authenticationAPI.service';
import { AuthenticationAPIService } from './authenticationAPI.service';
export * from './bookAPI.service';
import { BookAPIService } from './bookAPI.service';
export const APIS = [AuthenticationAPIService, BookAPIService];
