/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabfinalbd;

/**
 *
 * @author Adriller Ferreira
 */
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.mongodb.MongoClient;
import com.sun.xml.internal.ws.util.StringUtils;
import javax.swing.JTextArea;
import org.apache.commons.lang.math.NumberUtils;

public class ex4 {

    ActionListener Pesquisar(String tabelaAtual, JTextArea result, JTextArea query) {
        ActionListener temp = null;

        temp = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                result.setText("");
                try {
                    MongoClient mongoClient = new MongoClient("localhost", 27017);
                    DB db = mongoClient.getDB("test");
                    DBCollection coll = db.getCollection(tabelaAtual);
                    DBCursor cursor = null;
                    BasicDBObject whereQuery = null;
                    if (query.getText().isEmpty()) {
                        cursor = coll.find();
                    } else {
                        String delimins = "[=]+", attr = null, value = null, operator = null, aux = null;
                        String[] tokens = query.getText().split(delimins);
                        for (int i = 0; i < tokens.length; i = i + 3) {
                            attr = tokens[3 * i];
                            value = tokens[3 * i + 1];
                            //operator = tokens[3 * i + 1];
                            //value = tokens[3 * i + 2];
                            whereQuery = new BasicDBObject();
                            if (NumberUtils.isNumber(value)) {
                                int auxValue =Integer.parseInt(value);
                                //System.out.println(auxValue);
                                whereQuery.put(attr, value);
                            } else {
                                whereQuery.put(attr, value);
                            }
                            System.out.println(attr + " " + value);
                        }
                        //whereQuery.put("NROZONA", "18");
                        cursor = coll.find(whereQuery);
                    }
                    while (cursor.hasNext()) {
                        result.setText(result.getText() + "\n" +  cursor.next());
                        //System.out.println("aaa");
                    }
                    cursor.close();
                } catch (Exception e) {
                    System.out.println("erro na pesq");
                }
            }
        };
        return temp;
    }

}
