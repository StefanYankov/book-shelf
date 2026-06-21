export * from './authenticationAPI.service';
import { AuthenticationAPIService } from './authenticationAPI.service';
export * from './bookAPI.service';
import { BookAPIService } from './bookAPI.service';
export * from './userShelfAPI.service';
import { UserShelfAPIService } from './userShelfAPI.service';
export const APIS = [AuthenticationAPIService, BookAPIService, UserShelfAPIService];
