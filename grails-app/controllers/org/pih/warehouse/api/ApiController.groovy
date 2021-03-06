/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.api

import grails.converters.JSON
import org.hibernate.ObjectNotFoundException
import org.pih.warehouse.core.Location
import org.pih.warehouse.core.User
import org.pih.warehouse.product.Product

class ApiController {

    def dataSource
    def userService

    def login = {
        def username = request.JSON.username
        def password = request.JSON.password
        if (userService.authenticate(username, password)) {
            session.user = User.findByUsernameOrEmail(username, username)
            if (request.JSON.location) {
                session.warehouse = Location.get(request.JSON.location)
            }
            render ([status: 200, text: "Authentication was successful"])
            return
        }
        render([status: 401, text: "Authentication failed"])
    }

    def chooseLocation = {
        Location location = Location.get(params.id)
        if (!location) {
            throw new ObjectNotFoundException(params.id, Location.class.toString())
        }
        session.warehouse = location
        render ([status: 200, text: "User ${session.user} is now logged into ${location.name}"])
    }

    def getSession = {
        User user = User.get(session?.user?.id)
        Location location = Location.get(session.warehouse?.id)
        render ([data:[user:user, location:location]] as JSON)
    }


    def logout = {
        session.invalidate()
        render ([status: 200, text: "Logout was successful"])
    }


	def status = {
        boolean databaseStatus = true
        String databaseStatusMessage = "Database is available"

        try {
            Product.count()
        } catch (Exception e) {
            databaseStatus = false
            databaseStatusMessage = "Error: " + e.message
        }
		render ([status: "OK", database: [status: databaseStatus, message: databaseStatusMessage?:""] ] as JSON)
	}
	
}
