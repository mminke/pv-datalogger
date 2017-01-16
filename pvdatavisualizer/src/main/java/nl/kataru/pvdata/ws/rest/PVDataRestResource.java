package nl.kataru.pvdata.ws.rest;

import nl.kataru.pvdata.PVDataService;
import org.bson.Document;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author morten
 *
 */
@Stateless
@Path("/api")
public class PVDataRestResource {

	@Inject
    private PVDataService pvDataService;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/inverters")
    @PermitAll
    public String getInverters() {
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

	/**
	 * Use POST to create a non idempotent method to create a new inverter.
	 *
     * @param input
     * @return
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/inverters")
	public Response createInverter(String input) {
		final Document newInverter = Document.parse(input);

		if (pvDataService.getInverter(newInverter.getString("serialnumber")) == null) {
			pvDataService.saveInverter(newInverter);
			return Response.ok().build();
		} else {
			// An inverter with the given serialnumber already exists.
			return Response.notAcceptable(null).build();
		}
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
