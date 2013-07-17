package bartsy

class UserRewardPoints {

	static constraints = {
	}

	static belongsTo = [user : UserProfile,venue : Venue,order:Orders]

	static mapping={ rewardPoints defaultValue: 0 }

	int rewardPoints
	Date dateCreated
}
