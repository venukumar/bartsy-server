package bartsy

import java.util.Date;

class UserCheckInDetails {

	static constraints = {}

	static belongsTo = [userProfile : UserProfile,venue : Venue]

	Date checkedInDate
}
