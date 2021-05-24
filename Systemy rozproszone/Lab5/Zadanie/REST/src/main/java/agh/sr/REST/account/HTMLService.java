package agh.sr.REST.account;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class HTMLService {
    public String parseHtml(String htmlFile) {
        StringBuilder sb = new StringBuilder();

        try(BufferedReader in = new BufferedReader(new FileReader("src/main/resources/static/" + htmlFile))){
            String str;
            while ((str = in.readLine()) != null)
                sb.append(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public String addDiv(String htmlString, String newDiv, int newDivIdx){
        String[] mainAndEnd = htmlString.split("</body>");
        String[] divs = mainAndEnd[0].split("<div>");
        StringBuilder sb = new StringBuilder();
        boolean added = false;

        for(int i=0; i < divs.length; ++i){
            if(i == newDivIdx && !added){
                sb.append(newDiv);
                added = true;
                --i;
            }
            else {
                sb.append(divs[i]);
                if(i != divs.length - 1)    sb.append("<div>");
            }
        }

        if(!added)  sb.append(newDiv);
        return sb + "</body>" + mainAndEnd[1];
    }

    public String loginErrorHtml(){
        String basicLoginHtml = parseHtml("index.html");
        String errorDiv = "<div><h1>Error: Incorrect credentials</h1></div>";
        return addDiv(basicLoginHtml, errorDiv, 2);
    }

    public String registerMessageHtml(String message){
        String basicLoginHtml = parseHtml("index.html");
        String errorDiv = "<div><h1>" + message +"</h1></div>";
        return addDiv(basicLoginHtml, errorDiv, 3);
    }

    public String menuHtmlSite(String userEmail){
        String htmlMenu = parseHtml("menu.html");
        String loginDiv = "<div><h1>Logged as " + userEmail + " </h1></div>";
        return addDiv(htmlMenu, loginDiv, 2);
    }

    public String parseHistoryToHtml(ResultSet results) throws SQLException {
        StringBuilder sb = new StringBuilder();
        while(results.next()){
            String name = results.getString("name");
            int id = results.getInt("id");
            sb.append("<form action=\"/history/content\" method=\"get\"> " +
                    "<input type=\"hidden\" id=\"contentId\" name=\"contentId\" value=" + id + ">" +
                    "<input type=\"submit\" value=\"" + name + "\">" +
                    "</form>");
        }
        return sb.toString();
    }
}
