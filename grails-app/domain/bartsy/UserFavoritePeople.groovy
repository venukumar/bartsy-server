package bartsy

class UserFavoritePeople {

	String userBartsyId
	String favoriteBartsyId
	String staus//Like or Unlike

	static constraints = {
		staus(nullable:false)
		userBartsyId(nullable:false)
		favoriteBartsyId(nullable:false)
	}
	static belongsTo = [userProfile : UserProfile,venue : Venue]
}
