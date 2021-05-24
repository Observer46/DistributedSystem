package services.rest.RESTEST;

import services.rest.RESTEST.model.Todo;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Path("/todos")
public class TodoResource {

    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    String id;

    public TodoResource(UriInfo uriInfo, Request request, String id){
        this.uriInfo = uriInfo;
        this.request = request;
        this.id = id;
    }

//    @GET
//    @Produces({MediaType.TEXT_XML})
//    public Todo getTodoHTML(){
//        Todo todo = TodoDao.getModel().get(id);
//        return todo;
//    }
//
//    @GET
//    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
//    public Todo getTodo(){
//        Todo todo = TodoDao.getModel().get(id);
//        return todo;
//    }

    @PUT
    @Consumes(MediaType.APPLICATION_XML)
    public Response putTodo(JAXBElement<Todo> todo){
        Todo c = todo.getValue();
        return putAndGetResponse(c);
    }

    @DELETE
    public void deleteTodo(){
        Todo c = TodoDao.getModel().remove(id);
    }

    private Response putAndGetResponse(Todo todo){
        Response res;
        if (TodoDao.getModel().containsKey(todo.getId()))
            res = Response.noContent().build();
        else
            res = Response.created(uriInfo.getAbsolutePath()).build();
        TodoDao.getModel().put(todo.getId(), todo);
        return res;
    }

    @GET
    @Produces(MediaType.TEXT_XML)
    public List<Todo> getTodosBrowser() {
        List<Todo> todos = new ArrayList<Todo>();
        todos.addAll(TodoDao.getModel().values());
        return todos;
    }
    // Return the list of todos for applications
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public List<Todo> getTodos() {
        List<Todo> todos = new ArrayList<Todo>();
        todos.addAll(TodoDao.getModel().values());
        return todos;
    }
    // Returns the number of todos
    // Use http://localhost:8080/WS1/rest/todos/count
    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String getCount() {
        int count = TodoDao.getModel().size();
        return String.valueOf(count);
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void newTodo(@FormParam("id") String id,
                        @FormParam("summary") String summary,
                        @FormParam("description") String description,
                        @Context HttpServletResponse servletResponse) throws IOException {
        Todo todo = new Todo(id, summary);
        if (description != null) {
            todo.setDescription(description);
        }
        TodoDao.getModel().put(id, todo);
        servletResponse.sendRedirect("../index.html");
    }
    // Defines that the next path parameter after todos is
    // treated as a parameter and passed to the TodoResources
    // Allows to type http://localhost:8080/REST_Ex1/rest/todos/1
    // 1 will be treaded as parameter todo and passed to TodoResource
    @Path("{todo}")
    public TodoResource getTodo(@PathParam("todo") String id) {
        return new TodoResource(uriInfo, request, id);
    }
}
