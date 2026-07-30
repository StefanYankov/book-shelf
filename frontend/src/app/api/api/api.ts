export * from './adminAPI.service';
import {AdminAPIService} from './adminAPI.service';
import {AdminModerationAPIService} from './adminModerationAPI.service';
import {AuthenticationAPIService} from './authenticationAPI.service';
import {BookAPIService} from './bookAPI.service';
import {ReviewAPIService} from './reviewAPI.service';
import {UserProfileAPIService} from './userProfileAPI.service';
import {UserShelfAPIService} from './userShelfAPI.service';

export * from './adminModerationAPI.service';
export * from './authenticationAPI.service';
export * from './bookAPI.service';
export * from './reviewAPI.service';
export * from './userProfileAPI.service';
export * from './userShelfAPI.service';
export const APIS = [AdminAPIService, AdminModerationAPIService, AuthenticationAPIService, BookAPIService, ReviewAPIService, UserProfileAPIService, UserShelfAPIService];
