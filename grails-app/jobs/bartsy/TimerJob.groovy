package bartsy



class TimerJob {
	
	def timeoutService
	
    static triggers = {
      cron name: 'Timeout', cronExpression: "0 0/2 * * * ?"	  	
    }

    def execute() {
       //log.warn("scheduling the job")
	   timeoutService.timeout()
    }
}
