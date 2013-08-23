package bartsy

class UserRewardPoints {

	static constraints = {
	}

	static belongsTo = [order:Orders,venue:Venue,user:UserProfile]

	static mapping={
		rewardPoints defaultValue: 0
		bartsyPoints defaultValue: 0
	}

	int rewardPoints
	int bartsyPoints
	Date dateCreated
}
