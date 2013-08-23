package bartsy

class RedeemPoints {

    static constraints = {
    }
	static belongsTo = [order:Orders]
	
	int bartsyRedeemPoints
	int venueRedeemPoints
}
