export * from './adminAPI.service';
import {AdminAPIService} from './adminAPI.service';
import {AuthenticationAPIService} from './authenticationAPI.service';
import {BookAPIService} from './bookAPI.service';
import {ModerationAPIService} from './moderationAPI.service';
import {ReadingChallengeAPIService} from './readingChallengeAPI.service';
import {ReviewAPIService} from './reviewAPI.service';
import {UserProfileAPIService} from './userProfileAPI.service';
import {UserShelfAPIService} from './userShelfAPI.service';

export * from './authenticationAPI.service';
export * from './bookAPI.service';

export * from './moderationAPI.service';
export * from './readingChallengeAPI.service';
export * from './reviewAPI.service';
export * from './userProfileAPI.service';
export * from './userShelfAPI.service';

export const APIS = [AdminAPIService, AuthenticationAPIService, BookAPIService, ModerationAPIService, ReadingChallengeAPIService, ReviewAPIService, UserProfileAPIService, UserShelfAPIService];
