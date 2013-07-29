package bartsy

class UserFavoritePeople {

	static constraints = {
		staus(nullable:false)
		userBartsyId(nullable:false)
		favoriteBartsyId(nullable:false)
	}
	static belongsTo = [user : UserProfile,venue : Venue]

	String userBartsyId
	String favoriteBartsyId
	String staus//Like or Unlike

}
