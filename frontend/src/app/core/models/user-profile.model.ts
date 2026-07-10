/**
 * @fileoverview Defines the local, strongly-typed interface for the user profile data.
 */

/**
 * Represents the data structure for a user's public profile.
 *
 * NOTE: This interface is defined manually on the frontend.
 * The OpenAPI generator does not create a named model for this DTO because it is only
 * used as a direct response body for a single endpoint (`GET /api/users/me`) and not
 * as a reusable schema in a request body or other operations.
 *
 * This manual definition ensures type safety for the component that consumes the endpoint.
 */
export interface UserProfile {
  /** The unique identifier of the user. */
  id: string;

  /** The user's unique username. */
  username: string;

  /** The user's unique email address. */
  email: string;

  /** The user's first name. */
  firstName: string;

  /** The user's last name. */
  lastName: string;
}
