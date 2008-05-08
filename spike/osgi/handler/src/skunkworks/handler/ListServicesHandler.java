package skunkworks.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geotools.gtcatalog.Catalog;
import org.geotools.gtcatalog.Service;

public class ListServicesHandler extends HttpServlet {

	Catalog catalog;

	public ListServicesHandler(Catalog catalog) {
		this.catalog = catalog;
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		
		doList(request,response);
	}
	
	protected void doList(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		List services = catalog.members(null);
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		out.println("<html><body>");
		out.println(services.size() + " services found.<br>");
		
		for (Iterator itr = services.iterator(); itr.hasNext();) {
			Service service = (Service)itr.next();
			out.println("<a href=\"" + service.getIdentifier() + "\">"+service.getInfo(null).getTitle()+ "</a><br>");
		}
		
		
		out.println("</body></html>");
		out.close();
	}
}
