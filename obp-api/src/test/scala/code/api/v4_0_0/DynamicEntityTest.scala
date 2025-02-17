/**
Open Bank Project - API
Copyright (C) 2011-2018, TESOBE Ltd

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

Email: contact@tesobe.com
TESOBE Ltd
Osloerstrasse 16/17
Berlin 13359, Germany

This product includes software developed at
TESOBE (http://www.tesobe.com/)
  */
package code.api.v4_0_0

import code.api.ErrorMessage
import code.api.util.ApiRole._
import code.api.util.ApiVersion
import code.api.util.ErrorMessages._
import code.api.v4_0_0.APIMethods400.Implementations4_0_0
import code.dynamicEntity.DynamicEntityCommons
import code.entitlement.Entitlement
import com.github.dwickern.macros.NameOf.nameOf
import net.liftweb.json.Serialization.write
import org.scalatest.Tag
import code.api.util.APIUtil.OAuth._
import scala.collection.immutable.List

class DynamicEntityTest extends V400ServerSetup {

  /**
    * Test tags
    * Example: To run tests with tag "getPermissions":
    * 	mvn test -D tagsToInclude
    *
    *  This is made possible by the scalatest maven plugin
    */
  object VersionOfApi extends Tag(ApiVersion.v4_0_0.toString)
  object ApiEndpoint1 extends Tag(nameOf(Implementations4_0_0.createDynamicEntity))
  object ApiEndpoint2 extends Tag(nameOf(Implementations4_0_0.updateDynamicEntity))
  object ApiEndpoint3 extends Tag(nameOf(Implementations4_0_0.getDynamicEntities))
  object ApiEndpoint4 extends Tag(nameOf(Implementations4_0_0.deleteDynamicEntity))

  val rightEntity = DynamicEntityCommons(entityName = "FooBar", metadataJson =
    """
      |{
      |    "definitions": {
      |        "FooBar": {
      |            "required": [
      |                "name"
      |            ],
      |            "properties": {
      |                "name": {
      |                    "type": "string",
      |                    "example": "James Brown"
      |                },
      |                "number": {
      |                    "type": "integer",
      |                    "example": "698761728934"
      |                }
      |            }
      |        }
      |    }
      |}
      |""".stripMargin)
  // wrong metadataJson
  val wrongEntity = DynamicEntityCommons(entityName = "FooBar", metadataJson =
    """
      |{
      |    "definitions": {
      |        "FooBar": {
      |            "required": [
      |                "name"
      |            ],
      |            "properties": {
      |                "name_wrong": {
      |                    "type": "string",
      |                    "example": "James Brown"
      |                },
      |                "number": {
      |                    "type": "integer",
      |                    "example": "698761728934"
      |                }
      |            }
      |        }
      |    }
      |}
      |""".stripMargin) 


  feature("Add a DynamicEntity v4.0.4- Unauthorized access") {
    scenario("We will call the endpoint without user credentials", ApiEndpoint1, VersionOfApi) {
      When("We make a request v4.0.0")
      val request400 = (v4_0_0_Request / "management" / "dynamic_entities").POST
      val response400 = makePostRequest(request400, write(rightEntity))
      Then("We should get a 400")
      response400.code should equal(400)
      And("error should be " + UserNotLoggedIn)
      response400.body.extract[ErrorMessage].message should equal (UserNotLoggedIn)
    }
  }
  feature("Update a DynamicEntity v4.0.4- Unauthorized access") {
    scenario("We will call the endpoint without user credentials", ApiEndpoint2, VersionOfApi) {
      When("We make a request v4.0.0")
      val request400 = (v4_0_0_Request / "management" / "dynamic_entities"/ "some-method-routing-id").PUT
      val response400 = makePutRequest(request400, write(rightEntity))
      Then("We should get a 400")
      response400.code should equal(400)
      And("error should be " + UserNotLoggedIn)
      response400.body.extract[ErrorMessage].message should equal (UserNotLoggedIn)
    }
  }
  feature("Get DynamicEntities v4.0.4- Unauthorized access") {
    scenario("We will call the endpoint without user credentials", ApiEndpoint3, VersionOfApi) {
      When("We make a request v4.0.0")
      val request400 = (v4_0_0_Request / "management" / "dynamic_entities").GET
      val response400 = makeGetRequest(request400)
      Then("We should get a 400")
      response400.code should equal(400)
      And("error should be " + UserNotLoggedIn)
      response400.body.extract[ErrorMessage].message should equal (UserNotLoggedIn)
    }
  }
  feature("Delete the DynamicEntity specified by METHOD_ROUTING_ID v4.0.4- Unauthorized access") {
    scenario("We will call the endpoint without user credentials", ApiEndpoint4, VersionOfApi) {
      When("We make a request v4.0.0")
      val request400 = (v4_0_0_Request / "management" / "dynamic_entities" / "METHOD_ROUTING_ID").DELETE
      val response400 = makeDeleteRequest(request400)
      Then("We should get a 400")
      response400.code should equal(400)
      And("error should be " + UserNotLoggedIn)
      response400.body.extract[ErrorMessage].message should equal (UserNotLoggedIn)
    }
  }


  feature("Add a DynamicEntity v4.0.4- Unauthorized access - Authorized access") {
    scenario("We will call the endpoint without the proper Role " + canCreateDynamicEntity, ApiEndpoint1, VersionOfApi) {
      When("We make a request v4.0.0 without a Role " + canCreateTaxResidence)
      val request400 = (v4_0_0_Request / "management" / "dynamic_entities").POST <@(user1)
      val response400 = makePostRequest(request400, write(rightEntity))
      Then("We should get a 403")
      response400.code should equal(403)
      And("error should be " + UserHasMissingRoles + CanCreateDynamicEntity)
      response400.body.extract[ErrorMessage].message should equal (UserHasMissingRoles + CanCreateDynamicEntity)
    }

    scenario("We will call the endpoint with the proper Role " + canCreateDynamicEntity , ApiEndpoint1, ApiEndpoint2, ApiEndpoint3, ApiEndpoint4, VersionOfApi) {
      Entitlement.entitlement.vend.addEntitlement("", resourceUser1.userId, CanCreateDynamicEntity.toString)
      When("We make a request v4.0.0")
      val request400 = (v4_0_0_Request / "management" / "dynamic_entities").POST <@(user1)
      val response400 = makePostRequest(request400, write(rightEntity))
      Then("We should get a 201")
      response400.code should equal(201)
      val customerJson = response400.body.extract[DynamicEntityCommons]

      Entitlement.entitlement.vend.addEntitlement("", resourceUser1.userId, CanUpdateDynamicEntity.toString)
      When("We make a request v4.0.0 with the Role " + canUpdateDynamicEntity)

      {
        // update success
        val request400 = (v4_0_0_Request / "management" / "dynamic_entities" / customerJson.dynamicEntityId.get ).PUT <@(user1)
        val metadataJson = customerJson.metadataJson
        val entityName = customerJson.entityName
        val response400 = makePutRequest(request400, write(customerJson.copy(entityName = "Hello", metadataJson.replace(entityName, "Hello"))))
        Then("We should get a 200")
        response400.code should equal(200)
        val dynamicEntitiesJson = response400.body.extract[DynamicEntityCommons]
        dynamicEntitiesJson.entityName should be ("Hello")
      }

      {
        // update a not exists DynamicEntity
        val request400 = (v4_0_0_Request / "management" / "dynamic_entities" / "not-exists-id" ).PUT <@(user1)
        val response400 = makePutRequest(request400, write(customerJson))
        Then("We should get a 400")
        response400.code should equal(400)
        response400.body.extract[ErrorMessage].message should startWith (DynamicEntityNotFoundByDynamicEntityId)
      }

      {
        // update a DynamicEntity with wrong metadataJson
        val request400 = (v4_0_0_Request / "management" / "dynamic_entities" / customerJson.dynamicEntityId.get ).PUT <@(user1)
        val response400 = makePutRequest(request400, write(wrongEntity))
        Then("We should get a 400")
        response400.code should equal(400)
        response400.body.extract[ErrorMessage].message should startWith (InvalidJsonFormat)
      }

      Entitlement.entitlement.vend.addEntitlement("", resourceUser1.userId, CanGetDynamicEntities.toString)
      When("We make a request v4.0.0 with the Role " + canGetDynamicEntities)
      val requestGet400 = (v4_0_0_Request / "management" / "dynamic_entities").GET <@(user1)
      val responseGet400 = makeGetRequest(requestGet400)
      Then("We should get a 200")
      responseGet400.code should equal(200)
      val json = responseGet400.body \ "dynamic_entities"
      val dynamicEntitiesGetJson = json.extract[List[DynamicEntityCommons]]

      dynamicEntitiesGetJson.size should be (1)

      Entitlement.entitlement.vend.addEntitlement("", resourceUser1.userId, CanDeleteDynamicEntity.toString)
      When("We make a request v4.0.0 with the Role " + canDeleteDynamicEntity)
      val requestDelete400 = (v4_0_0_Request / "management" / "dynamic_entities" / dynamicEntitiesGetJson.head.dynamicEntityId.get).DELETE <@(user1)
      val responseDelete400 = makeDeleteRequest(requestDelete400)
      Then("We should get a 200")
      responseDelete400.code should equal(200)

    }
  }


}
