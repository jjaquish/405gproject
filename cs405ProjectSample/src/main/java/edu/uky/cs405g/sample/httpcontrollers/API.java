package edu.uky.cs405g.sample.httpcontrollers;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.uky.cs405g.sample.Launcher;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@Path("/api")
public class API {

    private Type mapType;
    private Gson gson;

    public API() {
        mapType = new TypeToken<Map<String, String>>() {
        }.getType();
        gson = new Gson();
    }


    //curl http://localhost:9998/api/check
    //{"status_code":1}
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response healthcheck() {

        String responseString = "{\"status_code\":0}";
        try {

            //Here is where you would put your system test, but this is not required.
            //We just want to make sure your API is up and active/
            //status_code = 0 , API is offline
            //status_code = 1 , API is online
            responseString = "{\"status_code\":1}";

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }


    //curl http://localhost:9998/api/listlocations
    //{"779a038b-aacc-44ca-b8cc-99671475061f":"800 Rose St.","1e4494a9-5677-49e4-b59f-b77c7900c73f":"123 Campus Road"}
    @GET
    @Path("/listlocations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listTeams() {
        String responseString = "{}";
        try {
            Map<String,String> teamMap = Launcher.dbEngine.getLocations();

            responseString = Launcher.gson.toJson(teamMap);

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    //curl http://localhost:9998/api/getlocation/800%20Rose%20St.
    //{"address":"800 Rose St.","lid":"c078b038-8ad2-4f45-adf0-03a22fffa8b9"}
    @GET
    @Path("/getlocation/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTeam(@PathParam("address") String address) {
        String responseString = "{}";
        try {

            Map<String,String> teamMap = Launcher.dbEngine.getLocation(address);

            responseString = Launcher.gson.toJson(teamMap);

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/getprovider/{npi}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addProv(@PathParam("npi") String npi) {
        String responseString = "{}";
        try {

            Map<String,String> providerMap = Launcher.dbEngine.getProvider(npi);

            responseString = Launcher.gson.toJson(providerMap);

        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/removeprovider/{npi}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProvider(@PathParam("npi") String npi) {
        String responseString = "{}";
        try {


            String queryString = "delete from provider WHERE npi='" + npi + "'";

            System.out.println(queryString);

            int status = Launcher.dbEngine.executeUpdate(queryString);

            System.out.println("status: " + status);

            responseString = "{\"status\":\"" + status +"\"}";


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/getservice/{service_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getService(@PathParam("service_id") String service_id) {
        String responseString = "{}";
        try {

            Map<String,String> serviceMap = Launcher.dbEngine.getService(service_id);
            if (serviceMap.size() != 0) {
                Map<String, String> locationMap = Launcher.dbEngine.getLocationFromId(serviceMap.get("location_id"));
                Map<String, String> departmentMap = Launcher.dbEngine.getDepartment(serviceMap.get("department_id"));
                Map<String, String> instMap = Launcher.dbEngine.getInstitutionFromId(departmentMap.get("institution_id"));
                Map<String, String> responseMap = new HashMap<>();
                
                responseMap.put("address", locationMap.get("address"));
                responseMap.put("department_id", serviceMap.get("department_id"));
                responseMap.put("taxid", instMap.get("taxid"));
                responseMap.put("service_id", serviceMap.get("id"));

                responseString = Launcher.gson.toJson(responseMap);
            }




        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    //curl http://localhost:9998/api/removelocation/ff2f86ba-ea87-4f5d-8d39-4bdd20b7a532
    //{"status":"1"}
    @GET
    @Path("/removelocation/{location_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteLocation(@PathParam("location_id") String locationId) {
        String responseString = "{}";
        try {


            String queryString = "delete from location WHERE lid='" + locationId + "'";

            System.out.println(queryString);

            int status = Launcher.dbEngine.executeUpdate(queryString);

            System.out.println("status: " + status);

            responseString = "{\"status\":\"" + status +"\"}";


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    //curl -d '{"address":"800 Rose St."}' -H "Content-Type: application/json" -X POST http://localhost:9998/api/addlocation
    //{"address":"800 Rose St.","lid":"ff2f86ba-ea87-4f5d-8d39-4bdd20b7a532"}
    @POST
    @Path("/addlocation")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crunchifyREST11(InputStream incomingData) {

        StringBuilder crunchifyBuilder = new StringBuilder();
        String returnString = null;
        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }

            String jsonString = crunchifyBuilder.toString();
            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String address = myMap.get("address");

            Map<String,String> addressMap = Launcher.dbEngine.getLocation(address);

            if(addressMap.size() == 0) {

                //generate a new unique location Id
                String locationId = UUID.randomUUID().toString();

                String createUsersTable = "insert into location values ('" + locationId + "','" + address  + "')";

                System.out.println(createUsersTable);

                Launcher.dbEngine.executeUpdate(createUsersTable);

                addressMap = Launcher.dbEngine.getLocation(address);

                returnString = gson.toJson(addressMap);


            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Can't insert duplicate location address!")
                        .header("Access-Control-Allow-Origin", "*").build();
            }


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error")
                    .header("Access-Control-Allow-Origin", "*").build();
        }

        return Response.ok(returnString).header("Access-Control-Allow-Origin", "*").build();
    }

    @POST
    @Path("/addprovider")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crunchifyREST13(InputStream incomingData) {

        StringBuilder crunchifyBuilder = new StringBuilder();
        String returnString = null;
        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }

            String jsonString = crunchifyBuilder.toString();
            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String npi = myMap.get("npi");
            String department_id = myMap.get("department_id");

            Map<String,String> departmentMap = Launcher.dbEngine.getDepartment(department_id);
            System.out.println(departmentMap.size());

            if(departmentMap.size() != 0) {

                //The department must exist

                Map<String, String> providerMap = Launcher.dbEngine.getProvider(npi);

                if (providerMap.size() == 0) {
                    //we're in business
                    String createUsersTable = "insert into provider values ('" + npi + "','" + department_id  + "')";

                    System.out.println(createUsersTable);

                    int status = Launcher.dbEngine.executeUpdate(createUsersTable);

                    System.out.println("status: " + status);

                    returnString = "{\"status\":\"" + status +"\"}";

                } else {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("The provided npi already exists!")
                    .header("Access-Control-Allow-Origin", "*").build();
                }
                




            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("The provided department_id doesn't exist!")
                        .header("Access-Control-Allow-Origin", "*").build();
            }


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error")
                    .header("Access-Control-Allow-Origin", "*").build();
        }

        return Response.ok(returnString).header("Access-Control-Allow-Origin", "*").build();
    }

    @POST
    @Path("/addservice")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crunchifyREST12(InputStream incomingData) {

        StringBuilder crunchifyBuilder = new StringBuilder();
        String returnString = null;
        try {

            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }

            String jsonString = crunchifyBuilder.toString();
            String dpInstId = "";
            Map<String, String> myMap = gson.fromJson(jsonString, mapType);

            String address = myMap.get("address");
            String departmentId = myMap.get("department_id");
            String serviceId = myMap.get("service_id");
            String taxId = myMap.get("taxid");

            //returnString = address + " - " + dptmtId + " - " + serviceId + " - " + taxId;

            Map<String,String> addressMap = Launcher.dbEngine.getLocation(address);
            Map<String, String> departmentMap = Launcher.dbEngine.getDepartment(departmentId);
            Map<String, String> serviceMap = Launcher.dbEngine.getService(serviceId);
            Map<String, String> taxMap = Launcher.dbEngine.getInstitution(taxId);

            
            //Everything depends on the service ID, because this is the only one that must be new.
            if(serviceMap.size() == 0) {

                //this means the location doesn't exist
                //and there isn't anything to worry about here.
                if (addressMap.size() == 0) {

                    //generate a new unique location ID
                    String locationId = UUID.randomUUID().toString();

                    String createLocationTable = "insert into location values ('" + locationId + "','" + address  + "')";

                    System.out.println(createLocationTable);

                    Launcher.dbEngine.executeUpdate(createLocationTable);

                    addressMap = Launcher.dbEngine.getLocation(address);
                }

                //There is some stuff to worry about with department.
                if (departmentMap.size() == 0) {

                    //The department doesn't exist in here.
                    //We want to know if the institution exists before
                    //we waste time making it.

                    if (taxMap.size() == 0) {
                        //The department doesn't exist and the institution doesn't exist in here.
                        //We have to have a valid institution ID before we make the department

                        //We generate a new institution ID
                        //But we already haver a valid taxId
                        dpInstId = UUID.randomUUID().toString();

                        String createInstitutionTable = "insert into institution values ('" + dpInstId +"', '" + taxId + "')";

                        System.out.println(createInstitutionTable);

                        Launcher.dbEngine.executeUpdate(createInstitutionTable);

                        taxMap = Launcher.dbEngine.getInstitution(taxId);

                    } else {
                        //The department doesn't exist but the institutiob does exist in here.
                        dpInstId = taxMap.get("id");
                    }

                    //Once we've gotten here, we should have a valid institution ID
                    //regardless of what else happened.


                    //At this point we can create the department entry
                    String createDepartmentTable = "insert into department values ('" + departmentId + "', '" + dpInstId + "')";

                    System.out.println(createDepartmentTable);

                    Launcher.dbEngine.executeUpdate(createDepartmentTable);

                    departmentMap = Launcher.dbEngine.getDepartment(departmentId);

                } else {
                //This means the department did exist
                //all we want to do in here is make sure that the taxID they
                //entered matches what is in the database for the department.
                    //Here is where we throw the error if the taxId doesn't match
                    String mytid = taxMap.get("id");
                    String myiid = departmentMap.get("institution_id");

                    if (taxMap.size() == 0) { // || taxMap.get("id") != departmentMap.get("institution_id")) {

                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("The taxId you entered doesn't exist even though the department does!")
                        .header("Access-Control-Allow-Origin", "*").build();
                    } else if(!mytid.equals(myiid)) {
                        System.out.println(mytid + "\n" + myiid);
                       return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("The taxId you entered doesn't match the pre-existing department!")
                        .header("Access-Control-Allow-Origin", "*").build(); 
                    }

                }

                //At this point departmentMap is ready to go
                //and taxMap is ready to go

                String createServiceTable = "insert into service values('" + serviceId + "', '" + addressMap.get("lid") + "', '" + departmentId + "')";

                Launcher.dbEngine.executeUpdate(createServiceTable);

                serviceMap = Launcher.dbEngine.getService(serviceId);

                returnString = "{\"status_code\":1}";


            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Can't insert duplicate service ID!")
                        .header("Access-Control-Allow-Origin", "*").build();
            }


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Server Error")
                    .header("Access-Control-Allow-Origin", "*").build();
        }

        return Response.ok(returnString).header("Access-Control-Allow-Origin", "*").build();
    }

}
