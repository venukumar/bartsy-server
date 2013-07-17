package bartsy


class UserService {

	CommonMethods common = new CommonMethods()
	def serviceMethod() {
	}
	def getuserRewards(bartsyId,response){
		println" Service "+bartsyId
		def Map map = new HashMap()
		try{
			def listOfVenues=[]
			def user = UserProfile.findByBartsyId(bartsyId)

			//def rewardsDetails=UserRewardPoints.findAllByUser(user)

			def rewardsDetails = UserRewardPoints.createCriteria().list() {
				eq("user", user)
				projections {
					sum("rewardPoints")
					groupProperty("venue")
				}
			}
			if(rewardsDetails){
				rewardsDetails.each{
					def userRewards = it
					def venueDetails=[:]
					def availableRewards=userRewards[0]
					def venue = userRewards[1]
					venueDetails.put("rewards",availableRewards)
					venueDetails.put("venueName",venue.venueName)
					venueDetails.put("venueId",venue.venueId)
					venueDetails.put("venueImage",venue.venueImagePath)
					venueDetails.put("address",venue.address)
					venueDetails.put("status",venue.status)
					listOfVenues.add(venueDetails)
				}
				map.put("errorCode","0")
				map.put("venues",listOfVenues)
			}else{
				map.put("errorMessage","No reward points available")
				map.put("errorCode","2")
			}
		}catch(Exception e){
			log.info("Exception found in getUserRewards "+e.getMessage())
			common.exceptionFound(e,map)
		}finally{
			map.put("currentTime",new Date().toGMTString())
			return map
		}
	}
}
