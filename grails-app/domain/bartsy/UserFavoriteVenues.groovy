package bartsy

class UserFavoriteVenues {

	static constraints = {
	}
	
	static belongsTo = [user : UserProfile,venue : Venue]
}
