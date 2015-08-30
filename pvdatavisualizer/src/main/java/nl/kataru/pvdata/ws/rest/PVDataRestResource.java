/**
 *
 */
package nl.kataru.pvdata.ws.rest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import nl.kataru.pvdata.PVDataService;

/**
 * @author morten
 *
 */
@Stateless
@Path("/api")
public class PVDataRestResource {

	@Inject
	PVDataService pvDataService;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/inverters")
	public String getInverter() {
		return "Hello World: All";
	}

	// TODO: Create POST method to create new inverter documents in MongoDB

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/inverters/{id}")
	public Response getInverter(@PathParam("id") String inverterId) {

		final String inverter = pvDataService.getInverter(inverterId);

		if (inverter == null) {
			return Response.status(404).build();
		}

		return Response.status(200).entity(inverter).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/inverters/{id}/data/actual")
	public Response getActualData(@PathParam("id") String inverterId) {

		final String actualData = pvDataService.getActualData(inverterId);

		if (actualData == null) {
			return Response.status(404).build();
		}

		return Response.status(200).entity(actualData).build();
	}
}
