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

	/**
	 *  To get venue rewards
	 */

	def getVenueRewards(Venue venue){
		def output=[:]
		try{
			def venueRewards = VenueConfig.findAllByVenue(venue)
			if(venueRewards){
				def rewards=[]
				venueRewards.each {
					def venueReward = it
					def rewardObj = [:]
					
					if(venueReward.type){
						rewardObj.put("type",venueReward.type)
					}
					if(venueReward.rewardPoints){
						rewardObj.put("points",venueReward.rewardPoints)
					}
					if(venueReward.value){
						rewardObj.put("value",venueReward.value)
					}
					if(venueReward.description){
						rewardObj.put("text",venueReward.description)
					}
					println"rewardObj "+rewardObj
					//if(rewardObj.)
					rewards.add(rewardObj)
				}
				println"rewards "+rewards
				output.put("errorCode",0)
				output.put("errorMessage","Venue rewards available")
				output.put("rewards",rewards)
			}else{
				output.put("errorCode",5)
				output.put("errorMessage","No venue rewards available")
			}
		}catch(Exception e){
			log.info("Exception found in getVenueRewards service "+e.getMessage())
			common.exceptionFound(e,output)
		}
		println"output "+output
		return output
	}

}
