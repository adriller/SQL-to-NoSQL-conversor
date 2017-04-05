/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabfinalbd;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.sql.DriverManager;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Adriller Ferreira
 */
public class ex1 {

    Connection connection;
    ArrayList<String> nomesTabelas = new ArrayList<>();
    String esquema;
    public int id = 0;
    Statement stmt;

    public ex1(final JTextField esq, final JPasswordField sen, ArrayList<String> tables) {
        nomesTabelas = tables;
        esquema = esq.getText();
        String senha = String.valueOf(sen.getPassword());
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@grad.icmc.usp.br:15215:orcl",
                    esquema,
                    senha);
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("Conexao Perdida");
        }
    }

    public ArrayList pegarColunasDaTabela(String nomeTabela) {
        String s = "";
        ResultSet rs;
        ArrayList<String> retorno = new ArrayList<>();
        try {
            s = "SELECT Column_name FROM user_tab_columns where table_name = '" + nomeTabela + "'";
            //System.out.println(s);
            stmt = connection.createStatement();
            rs = stmt.executeQuery(s);
            while (rs.next()) {
                //System.out.println("oi");
                retorno.add(rs.getString("column_name"));
                //System.out.println("coluna: " + rs.getString("column_name"));
            }
            stmt.close();
        } catch (SQLException ex) {
            //jtAreaDeStatus.setText("Erro na consulta: \"" + s + "\"");
        }
        return retorno;
    }

    public int consultarNumElem(String nomeTabela) {
        String s = "SELECT COUNT(*) AS total FROM user_tab_columns where table_name = '" + nomeTabela + "'";
        int num = 0;
        ResultSet rs;
        try {
            stmt = connection.createStatement();
            rs = stmt.executeQuery(s);
            while (rs.next()) {
                num = rs.getInt("total");

            }
            stmt.close();
        } catch (SQLException ex) {

        }
        return num;
    }

    public ArrayList pegarTiposDeColuna(String nomeTabela) {
        String s = "";
        ResultSet rs;
        ArrayList<String> retorno = new ArrayList<>();
        ArrayList<String> nomeCol = this.pegarColunasDaTabela(nomeTabela);
        for (int i = 0; i < nomeCol.size(); i++) {
            try {
                s = "SELECT Data_type FROM user_tab_columns where table_name = '" + nomeTabela + "' and column_name = '" + nomeCol.get(i) + "'";
                //System.out.println(s);
                stmt = connection.createStatement();
                rs = stmt.executeQuery(s);
                while (rs.next()) {
                    //System.out.println("oi");
                    retorno.add(rs.getString("Data_type"));
                    //System.out.println("coluna: " + rs.getString("column_name"));
                }
                stmt.close();
            } catch (SQLException ex) {
                //jtAreaDeStatus.setText("Erro na consulta: \"" + s + "\"");
            }
        }
        return retorno;
    }

    public ArrayList<String> PegarPKs(String nomeTabela) {
        String s = "";
        ArrayList<String> retorno = new ArrayList<>();
        try {
            //s = "select COLUMN_NAME  from INFORMATION_SCHEMA.KEY_COLUMN_USAGE where WHERE OBJECTPROPERTY(OBJECT_ID(CONSTRAINT_SCHEMA+'.'+CONSTRAINT_NAME), 'IsPrimaryKey') = 1 AND TABLE_NAME = '" + nomeTabela + "'";
            //System.out.println(s);

            //stmt = connection.createStatement();
            //stmt.executeUpdate("create table survey (id int,name varchar, PRIMARY KEY (id) );");
            //stmt.executeUpdate("insert into survey (id,name ) values (1,'nameValue')");
            DatabaseMetaData meta = connection.getMetaData();
            //System.out.println("pegandoPK");
            ResultSet rs;
            rs = meta.getExportedKeys("", "", nomeTabela);
            while (rs.next()) {
                //System.out.println("pegandoPK1");
                String pk = rs.getString("PKCOLUMN_NAME");
                if (!retorno.contains(pk)) {
                    retorno.add(pk);
                    //System.out.println(pk);
                }
            }
            stmt.close();
        } catch (SQLException ex) {
            System.out.println("erro ao pegar PK");
        }
        return retorno;
    }

    public ArrayList<String> pegarReferences(String tabelaAtual) {
        ArrayList<String> retorno = new ArrayList<>();
        String query = "SELECT a.table_name, a.column_name AS COLN, a.constraint_name, c.owner, \n"
                + "       -- referenced pk\n"
                + "       c.r_owner, c_pk.table_name r_table_name, c_pk.constraint_name r_pk\n"
                + "  FROM all_cons_columns a\n"
                + "  JOIN all_constraints c ON a.owner = c.owner\n"
                + "                        AND a.constraint_name = c.constraint_name\n"
                + "  JOIN all_constraints c_pk ON c.r_owner = c_pk.owner\n"
                + "                           AND c.r_constraint_name = c_pk.constraint_name\n"
                + " WHERE c.constraint_type = 'R'\n"
                + "   AND a.table_name = '" + tabelaAtual + "'";
        //int i = 0;
        ResultSet rs = this.Query(query);
        //System.out.println("oi");
        try {
            while (rs.next()) {
                String tableName = rs.getString("r_table_name");
                String colName = rs.getString("COLN");
                if (!retorno.contains(tableName)) {
                    retorno.add(tableName);
                    retorno.add(colName);
                }

                //System.out.println(retorno.get(i));
                //System.out.println(retorno.get(i + 1));
                //i++;
            }
            stmt.close();
        } catch (Exception e) {
            System.out.println("Problema ao pegar References");
        }
        return retorno;
    }

    public ArrayList<String> pegarID(String tabelaAtual) {
        ArrayList<String> retorno = new ArrayList<>();
        String query = "SELECT a.table_name, a.column_name AS COLN, a.constraint_name, c.owner, \n"
                + "       -- referenced pk\n"
                + "       c.r_owner, c_pk.table_name r_table_name, c_pk.constraint_name r_pk\n"
                + "  FROM all_cons_columns a\n"
                + "  JOIN all_constraints c ON a.owner = c.owner\n"
                + "                        AND a.constraint_name = c.constraint_name\n"
                + "  JOIN all_constraints c_pk ON c.r_owner = c_pk.owner\n"
                + "                           AND c.r_constraint_name = c_pk.constraint_name\n"
                + " WHERE c.constraint_type = 'R'\n"
                + "   AND a.table_name = '" + tabelaAtual + "'";
        //int i = 0;
        ResultSet rs = this.Query(query);
        //System.out.println("oi");
        try {
            while (rs.next()) {
                String tableName = rs.getString("r_table_name");
                String colName = rs.getString("COLN");
                retorno.add(tableName);
                retorno.add(colName);

                //System.out.println(retorno.get(i));
                //System.out.println(retorno.get(i + 1));
                //i++;
            }
            stmt.close();
        } catch (Exception e) {
            System.out.println("Problema ao pegar References");
        }
        return retorno;
    }

    public ResultSet Query(String s) {
        ResultSet set;
        set = null;
        try {
            stmt = connection.createStatement();
            set = stmt.executeQuery(s);
        } catch (Exception e) {
            System.out.println("query nao executada");
        }

        return set;
    }

    public String makeTupla(ArrayList<String> colunas, ArrayList<String> values, ArrayList<String> primaryKeys, ArrayList<String> tipos, boolean embeeding, String tabelaAtual) {
        String tupla = "db." + tabelaAtual + ".insert({", tuplaEmbeeding = "", pks = "", tuplaupdate = "";
        ArrayList<String> references = this.pegarReferences(tabelaAtual), referencesID = this.pegarID(tabelaAtual), auxColunas = this.pegarColunasDaTabela(tabelaAtual), auxValues = new ArrayList<>(), auxTipos = new ArrayList<>();
        int i, j, numElem;

        for (i = 0; i < values.size(); i++) {
            auxValues.add(values.get(i));
            auxTipos.add(tipos.get(i));
        }
        if (embeeding) {
            tuplaEmbeeding = ex1.this.makeEmbeeding(colunas, values, tipos, tabelaAtual);
        }

        ArrayList<String> PKColunas = new ArrayList<>();
        ArrayList<String> PKValues = new ArrayList<>();
        ArrayList<String> PKTipos = new ArrayList<>();
        numElem = values.size();
        for (i = 0, j = 0; i < numElem; i++, j++) {
            if (primaryKeys.contains(colunas.get(j))) {
                //System.out.println("encontrado chave primaria " + colunas.get(j) + " " + values.get(j) + " " + tipos.get(j));
                PKColunas.add(colunas.get(j));
                PKValues.add(values.get(j));
                PKTipos.add(tipos.get(j));
                colunas.remove(j);
                values.remove(j);
                tipos.remove(j);
                j--;
            }
        }
        boolean possuiPK = PKValues.size() > 0;
        if (possuiPK) {
            pks = ex1.this.insertPK(PKColunas, PKValues, PKTipos);
            tupla = tupla + pks;
        } else {
            tupla = tupla + "_id: " + id + ", ";
            id++;
        }

        //System.out.println(tupla);
        if (embeeding) {
            //System.out.println("faz embeeding");
            tupla = tupla + ", " + tuplaEmbeeding;

        }
        numElem = values.size();
        if (numElem > 0 && possuiPK) {
            tupla = tupla + ", ";
        }
        for (i = 0; i < numElem; i++) {
            //System.out.println("encontrado  " + colunas.get(i) + " " + values.get(i) + " " + tipos.get(i));
            if (tipos.get(i).toUpperCase().contains("CHAR")) {
                tupla = tupla + colunas.get(i) + ": \"" + values.get(i) + "\"";

            } else if (tipos.get(i).toUpperCase().contains("DATE")) {
                //System.out.println("isDate");
                tupla = tupla + colunas.get(i) + ": \"" + values.get(i).substring(0, 11) + "\"";
            } else {
                tupla = tupla + colunas.get(i) + ": " + values.get(i);
            }
            if (i + 1 < numElem) {
                tupla = tupla + ", ";
            }
        }

        int numRef = references.size() / 2;

        if (numRef > 0 && !embeeding) {
            boolean doRef = true;
            String aux = "";
            for (int k = 0; k < referencesID.size(); k = k + 2) {
                if (aux.equals(referencesID.get(k))) {
                    doRef = false;
                }
                aux = referencesID.get(k);
            }
            if (doRef) {
                int auxIndex = auxColunas.indexOf(references.get(1));
                String auxPossui = auxValues.get(auxIndex), tuplaDate = "";
                if (auxPossui != null) {
                    //System.out.println("nao null");
                    tuplaupdate = tuplaupdate + "var doc = db." + references.get(0) + ".findOne({";
                    if (auxTipos.get(auxIndex).toUpperCase().contains("CHAR")) {
                        tuplaupdate = tuplaupdate + "_id: \"" + auxValues.get(auxColunas.indexOf(references.get(1))) + "\"})\n";
                    } else if (auxTipos.get(auxIndex).toUpperCase().contains("DATE")) {
                        //System.out.println("isDate");
                        tuplaupdate = tuplaupdate + "_id: \"" + auxValues.get(auxColunas.indexOf(references.get(1))).substring(0, 11) + "\"})\n";
                    } else {
                        tuplaupdate = tuplaupdate + "_id: " + auxValues.get(auxColunas.indexOf(references.get(1))) + "})\n";
                    }
                    tupla = tupla + ", " + references.get(0) + ": doc._id";
                    tupla = tuplaupdate + tupla;
                } else {
                    //System.out.println("null");
                }

            }

        }

        tupla = tupla + "}";
        //System.out.println(tupla);
        return tupla;
    }

    public String insertPK(ArrayList<String> PKColunas, ArrayList<String> PKValues, ArrayList<String> PKTipos) {
        int i, contpk = 0, numChaves = PKValues.size();
        String pk = "";

        if (numChaves > 1) {
            pk = "_id: {";
            for (i = 0; i < numChaves; i++) {
                if (PKTipos.get(i).toUpperCase().contains("CHAR")) {
                    pk = pk + PKColunas.get(i) + ": \"" + PKValues.get(i) + "\"";
                } else {
                    pk = pk + PKColunas.get(i) + ": " + PKValues.get(i);
                }
                if (contpk + 1 < numChaves) {
                    pk = pk + ", ";
                } else {
                    pk = pk + "}";
                }
                contpk++;
            }
        } else {
            //System.out.println("uma PK");
            pk = "_id: ";

            if (PKTipos.get(0).toUpperCase().contains("CHAR")) {
                pk = pk + "\"" + PKValues.get(0) + "\"";
            } else {
                pk = pk + PKValues.get(0);
            }
        }

        return pk;
    }

    public String makeEmbeeding(ArrayList<String> colunas, ArrayList<String> values, ArrayList<String> tipos, String tabelaAtual) {
        String value, referenced, attrOfRef = "", attrOfTA = "oi", tuplaReferenced = "";
        ArrayList<String> references = this.pegarReferences(tabelaAtual);

        int index, i = 0, numRef = references.size() / 2;
        if (numRef == 0) {
            return "";
        }
        for (i = 0; i < numRef; i++) {
            referenced = references.get(2 * i);
            attrOfTA = references.get(2 * i + 1);
            ArrayList<String> colunasReferenced = this.pegarColunasDaTabela(referenced);
            ArrayList<String> tiposReferenced = this.pegarTiposDeColuna(referenced);

            for (int j = 0; j < colunasReferenced.size(); j++) {
                if (attrOfTA.toUpperCase().contains(colunasReferenced.get(j).toUpperCase())) {

                    attrOfRef = colunasReferenced.get(j);
                }
            }

            int nElem = this.consultarNumElem(referenced);
            ArrayList<String> valuesReferenced = new ArrayList<>();
            index = colunas.indexOf(attrOfTA);
            tuplaReferenced = tuplaReferenced + referenced + ": {";

            String query;
            if (tipos.get(index).contains("CHAR")) {
                query = "SELECT * FROM " + referenced + " WHERE " + attrOfRef + " = \'" + values.get(index) + "\'";
            } else {
                query = "SELECT * FROM " + referenced + " WHERE " + attrOfRef + " = " + values.get(index);
            }
            ResultSet rs;
            rs = Query(query);
            try {
                while (rs.next()) {
                    for (int j = 0; j < nElem; j++) {
                        value = rs.getString(j + 1);
                        if (tiposReferenced.get(j).contains("CHAR")) {
                            tuplaReferenced = tuplaReferenced + colunasReferenced.get(j) + ": \"" + rs.getString(j + 1) + "\"";
                        } else {
                            tuplaReferenced = tuplaReferenced + colunasReferenced.get(j) + ": " + rs.getString(j + 1);
                        }
                        if (j + 1 < nElem) {
                            tuplaReferenced = tuplaReferenced + ", ";
                        }
                    }
                    tuplaReferenced = tuplaReferenced + "}";
                }
                stmt.close();
            } catch (Exception e) {
                System.out.println("erro ao gerar tupla");
            }
            //System.out.println(i);

            tuplaReferenced = tuplaReferenced + ", ";

        }

        //System.out.println("tuplaestado = " + tuplaestado);
        return tuplaReferenced;

    }

    public ActionListener GerarBSON(JTextArea tfGerador, final String tabelaAtual, JPanel JPex1, final JCheckBox embeeding, ArrayList<String> naoPossuiEmbed) {
        ActionListener temp = null;
        final int nElem = this.consultarNumElem(tabelaAtual);
        final ArrayList<String> colunas, tipos, primaryKeys, colunasEstado, tiposEstado;
        colunas = this.pegarColunasDaTabela(tabelaAtual);
        tipos = this.pegarTiposDeColuna(tabelaAtual);
        primaryKeys = this.PegarPKs(tabelaAtual);

        temp = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                boolean isSelected = embeeding.isSelected();
                ArrayList<String> values = new ArrayList<>(), auxColunas = new ArrayList<>(), auxTipos = new ArrayList<>(), auxPrimaryKeys = new ArrayList<>();
                String s = "SELECT * FROM " + tabelaAtual, value, meiodoc = "", iniciodocument = "db.createCollection(\"" + tabelaAtual + "\")\n", tupla = "db.\"" + tabelaAtual + "\".insert(", tuplaEmbeeding;
                ResultSet rs;
                int i;
                boolean doEmbeeding = embeeding.isSelected();
                if (doEmbeeding && naoPossuiEmbed.contains(tabelaAtual)) {
                    System.out.println("Essa Tabela nao sera criada com Embeeding");
                    return;
                }

                rs = Query(s);
                try {
                    boolean nxt = rs.next();

                    while (nxt) {
                        tupla = "";
                        values.clear();
                        auxColunas.clear();
                        auxTipos.clear();
                        auxPrimaryKeys.clear();
                        //System.out.println("preenchendo values");
                        for (i = 0; i < nElem; i++) {
                            value = rs.getString(i + 1);
                            values.add(value);
                        }
                        //System.out.println("fazendo copia");
                        for (i = 0; i < values.size(); i++) {
                            auxColunas.add(colunas.get(i));
                            auxTipos.add(tipos.get(i));
                        }
                        for (i = 0; i < primaryKeys.size(); i++) {
                            auxPrimaryKeys.add(primaryKeys.get(i));
                        }
                        // System.out.println("fazendo tupla");
                        try {
                            if (doEmbeeding && tabelaAtual.equals("LE02CIDADE")) {
                                tupla = tupla + ex1.this.makeTupla(auxColunas, values, auxPrimaryKeys, auxTipos, true, tabelaAtual);
                            } else {
                                tupla = tupla + ex1.this.makeTupla(auxColunas, values, auxPrimaryKeys, auxTipos, false, tabelaAtual);
                            }
                        } catch (Exception e) {
                            System.out.println("erro ao gerar tupla");
                        }
                        //System.out.println("Tupla Criada");
                        nxt = rs.next();
                        //if (nxt) {
                        //    tupla = tupla + ", \n";
                        //}
                        tupla = tupla + ")\n";
                        meiodoc = meiodoc + tupla;
                        //System.out.println("prox tupla");
                    }

                    meiodoc = meiodoc + "\n";
                    String document = iniciodocument + meiodoc;
                    //System.out.println(document);
                    tfGerador.setText(document);
                    stmt.close();
                } catch (Exception e) {
                    System.out.println("Erro ao gerar Doc");
                }
                JPex1.revalidate();
                JPex1.repaint();
            }

        };
        return temp;
    }

    public String GerarBSON1Tabela(final String tabelaAtual, final JCheckBox embeeding) {
        final int nElem = this.consultarNumElem(tabelaAtual);
        final ArrayList<String> colunas, tipos, primaryKeys, colunasEstado, tiposEstado;
        colunas = this.pegarColunasDaTabela(tabelaAtual);
        tipos = this.pegarTiposDeColuna(tabelaAtual);
        primaryKeys = this.PegarPKs(tabelaAtual);

        boolean isSelected = embeeding.isSelected();
        ArrayList<String> values = new ArrayList<>(), auxColunas = new ArrayList<>(), auxTipos = new ArrayList<>(), auxPrimaryKeys = new ArrayList<>();

        String document = "", s = "SELECT * FROM " + tabelaAtual, value, meiodoc = "", iniciodocument = "db.createCollection(\"" + tabelaAtual + "\")\n", tupla = "db.\"" + tabelaAtual + "\".insert(", tuplaEmbeeding;
        ResultSet rs;
        int i;

        boolean doEmbeeding = embeeding.isSelected();

        rs = Query(s);
        try {
            boolean nxt = rs.next();

            while (nxt) {
                tupla = "";
                values.clear();
                auxColunas.clear();
                auxTipos.clear();
                auxPrimaryKeys.clear();
                //System.out.println("preenchendo values");
                for (i = 0; i < nElem; i++) {
                    value = rs.getString(i + 1);
                    values.add(value);
                }
                //System.out.println("fazendo copia");
                for (i = 0; i < values.size(); i++) {
                    auxColunas.add(colunas.get(i));
                    auxTipos.add(tipos.get(i));
                }
                for (i = 0; i < primaryKeys.size(); i++) {
                    auxPrimaryKeys.add(primaryKeys.get(i));
                }
                // System.out.println("fazendo tupla");
                try {
                    if (doEmbeeding && tabelaAtual.equals("LE02CIDADE")) {
                        tupla = tupla + ex1.this.makeTupla(auxColunas, values, auxPrimaryKeys, auxTipos, true, tabelaAtual);
                    } else {
                        tupla = tupla + ex1.this.makeTupla(auxColunas, values, auxPrimaryKeys, auxTipos, false, tabelaAtual);
                    }
                } catch (Exception e) {
                    System.out.println("erro ao gerar tupla");
                }
                //System.out.println("Tupla Criada");
                nxt = rs.next();
                //if (nxt) {
                //    tupla = tupla + ", \n";
                //}
                tupla = tupla + ")\n";
                meiodoc = meiodoc + tupla;
                //System.out.println("prox tupla");
            }

            meiodoc = meiodoc + "\n";
            document = iniciodocument + meiodoc;
            //System.out.println(document);
            stmt.close();
        } catch (Exception e) {
            System.out.println("Erro ao gerar Doc");
        }

        return document;
    }

    ActionListener CriarScript(JButton criarScript, JPanel JPex1, JCheckBox embeeding, ArrayList<String> naoPossuiEmbed) {
        ActionListener temp = null;

        temp = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String script = "";
                try {
                    FileWriter arq = new FileWriter(new File("script.txt"));
                    PrintWriter gravarArq = new PrintWriter(arq);

                    for (int i = 6; i < nomesTabelas.size(); i++) {
                        System.out.println(nomesTabelas.get(i));
                        if (naoPossuiEmbed.contains(nomesTabelas.get(i)) && embeeding.isSelected()) {
                            System.out.println("Essa tabela não sera criada com embeeding");
                        } else {
                            //System.out.println(nomesTabelas.get(i));
                            script = script + "\n\n\n" + ex1.this.GerarBSON1Tabela(nomesTabelas.get(i), embeeding);
                        }

                    }

                    System.out.println("escrevendo...");
                    gravarArq.printf(script);
                    arq.close();
                    criarScript.setText("Criado em .txt!");
                } catch (Exception e) {
                    criarScript.setText("Erro");
                }

                JPex1.revalidate();
                JPex1.repaint();
            }

        };
        return temp;
    }

    public ArrayList<String> pegarUniques(String tabelaAtual) {
        ArrayList<String> retorno = new ArrayList<>();
        String query = "SELECT a.table_name, a.column_name AS COLN, a.constraint_name, c.owner, \n"
                + "       -- referenced pk\n"
                + "       c.r_owner, c_pk.table_name r_table_name, c_pk.constraint_name r_pk\n"
                + "  FROM all_cons_columns a\n"
                + "  JOIN all_constraints c ON a.owner = c.owner\n"
                + "                        AND a.constraint_name = c.constraint_name\n"
                + "  JOIN all_constraints c_pk ON c.r_owner = c_pk.owner\n"
                + "                           AND c.r_constraint_name = c_pk.constraint_name\n"
                + " WHERE c.constraint_type = 'u'\n"
                + "   AND a.table_name = '" + tabelaAtual + "'";
        //int i = 0;
        ResultSet rs = this.Query(query);
        //System.out.println("oi");
        try {
            while (rs.next()) {
                String colName = rs.getString("COLN");

                retorno.add(colName);

                //System.out.println(retorno.get(i));
                //System.out.println(retorno.get(i + 1));
                //i++;
            }
            stmt.close();
        } catch (Exception e) {
            System.out.println("Problema ao pegar References");
        }
        return retorno;
    }

    public ArrayList<String> getFks(String tabelaAtual) {
        ArrayList<String> retorno = new ArrayList<>();
        String query = "SELECT a.table_name, a.column_name AS COLN, a.constraint_name, c.owner, \n"
                + "       -- referenced pk\n"
                + "       c.r_owner, c_pk.table_name r_table_name, c_pk.constraint_name r_pk\n"
                + "  FROM all_cons_columns a\n"
                + "  JOIN all_constraints c ON a.owner = c.owner\n"
                + "                        AND a.constraint_name = c.constraint_name\n"
                + "  JOIN all_constraints c_pk ON c.r_owner = c_pk.owner\n"
                + "                           AND c.r_constraint_name = c_pk.constraint_name\n"
                + " WHERE c.constraint_type = 'R'\n"
                + "   AND a.table_name = '" + tabelaAtual + "'";
        //int i = 0;
        ResultSet rs = this.Query(query);
        //System.out.println("oi");
        try {
            while (rs.next()) {
                String tableName = rs.getString("r_table_name");
                String colName = rs.getString("COLN");
                retorno.add(colName);

                //System.out.println(retorno.get(i));
                //System.out.println(retorno.get(i + 1));
                //i++;
            }
            stmt.close();
        } catch (Exception e) {
            System.out.println("Problema ao pegar References");
        }
        return retorno;
    }

    public ArrayList<String> getR_table(String tabelaAtual) {
        ArrayList<String> retorno = new ArrayList<>();
        String query = "SELECT a.table_name, a.column_name AS COLN, a.constraint_name, c.owner, \n"
                + "       -- referenced pk\n"
                + "       c.r_owner, c_pk.table_name r_table_name, c_pk.constraint_name r_pk\n"
                + "  FROM all_cons_columns a\n"
                + "  JOIN all_constraints c ON a.owner = c.owner\n"
                + "                        AND a.constraint_name = c.constraint_name\n"
                + "  JOIN all_constraints c_pk ON c.r_owner = c_pk.owner\n"
                + "                           AND c.r_constraint_name = c_pk.constraint_name\n"
                + " WHERE c.constraint_type = 'R'\n"
                + "   AND a.table_name = '" + tabelaAtual + "'";
        //int i = 0;
        ResultSet rs = this.Query(query);
        //System.out.println("oi");
        try {
            while (rs.next()) {
                String tableName = rs.getString("r_table_name");
                String colName = rs.getString("COLN");
                retorno.add(tableName);

                //System.out.println(retorno.get(i));
                //System.out.println(retorno.get(i + 1));
                //i++;
            }
            stmt.close();
        } catch (Exception e) {
            System.out.println("Problema ao pegar References");
        }
        return retorno;
    }

    public ActionListener montaIndex(String tabelaAtual, JTextArea textIndices) {
        ActionListener temp = null;
        ArrayList<String> Fks = this.getFks(tabelaAtual);
        ArrayList<String> R_table = this.getR_table(tabelaAtual);
        ArrayList<String> uniques = this.pegarUniques(tabelaAtual);

        temp = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String indices = "\ndb.LE08CANDIDATO.remove({CPF: null}) \n"
                        + "\n"
                        + "db.LE01ESTADO.createIndex( { \"NOME\": 1 }, { unique: true } )\n"
                        + "\n"
                        + "db.LE04BAIRRO.createIndex( { \"CEP\": 1 }, { unique: true } )\n"
                        + "\n"
                        + "db.LE07PARTIDO.createIndex( { \"NOME\": 1}, { unique: true } )\n"
                        + "\n"
                        + "db.LE08CANDIDATO.createIndex( { \"CPF\": 1}, { unique: true } )\n"
                        + "\n";
                textIndices.setText(indices);

                try {
                    FileWriter arq = new FileWriter(new File("indices.txt"));
                    PrintWriter gravarArq = new PrintWriter(arq);
                    gravarArq.printf(indices);
                    arq.close();
                    System.out.println("Criado em .txt!");
                } catch (Exception e) {
                    System.out.println("impossivel escrever arq");
                }
                /*String mongoString = "db." + tabelaAtual + ".createIndex({";
                boolean isFk = false;
                String rTable = "";

                for (int i = 0; i < uniques.size(); i++) {
                    String aux = uniques.get(i);
                    if (Fks.contains(aux)) {
                        if (!isFk) {
                            isFk = true;
                            rTable = R_table.get(i);
                            if (i == 0) {
                                mongoString += rTable + "._id: 1";
                            } else {
                                mongoString += ", " + rTable + "._id: 1";
                            }
                        }
                    } else if (i == 0) {
                        mongoString += aux + ": 1";
                    } else {
                        mongoString += ", " + aux + ": 1";
                    }
                }
                mongoString += "} , {unique : true})";
                System.out.println(mongoString);*/
            }

        };
        return temp;
    }

    void EscreverChecks(JTextArea textCheck) {

        String text = "db.runCommand({\n"
                + "	collMod: \"LE02CIDADE\",\n"
                + "	validator: { \n"
                + "         $and: [\n"
                + "		{NOME: { $type: \"string\" }},\n"
                + "		{\"SIGLAESTADO\": { $type: \"string\" }}, \n"
                + "		{POPULACAO: { $type: \"int\" }, { $gte: 0 }}\n"
                + "        ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE03ZONA\",\n"
                + "	validator: { \n"
                + "	$and: [\n"
                + "		{_id: { $type: \"int\" }}, \n"
                + "		{NRODEURNASRESERVAS: { $type: \"int\" }}\n"
                + "     ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE04BAIRRO\",\n"
                + "	validator: { \n"
                + "         $and: [\n"
                + "		{_id: { $type: \"string\" }}, \n"
                + "		{\"Zona._id\": { $type: \"int\" }}\n"
                + "         ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE05URNA\",\n"
                + "	validator: { \n"
                + "         $and: [\n"
                + "		{_id: { $type: \"int\" }}, \n"
                + "		{Estado: { $type: \"string\" }, { $in: [\"funcional\", \"manutencao\"] }}\n"
                + "         ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE06SESSAO\",\n"
                + "	validator: { \n"
                + "	$and: [\n"
                + "		{_id: { $type: \"int\" }},\n"
                + "		{\"NSerial._id\": { $type: \"int\" }}\n"
                + "     ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE07PARTIDO\",\n"
                + "	validator: { \n"
                + "	$and: [\n"
                + "		{_id: { $type: \"string\" }},\n"
                + "		{Nome: { $type: \"string\" }}\n"
                + "     ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE08CANDIDATO\",\n"
                + "	validator: { \n"
                + "	$and: [\n"
                + "		{_id: { $type: \"int\" }}, \n"
                + "		{Tipo: { $type: \"string\" }},\n"
                + "		{Nome: { $type: \"string\" }},\n"
                + "               { $or: [\n"
                + "                     {{Tipo: { $in: [\"politico\"] }}, {SiglaPartido: { $type: \"string\" }}, {CPF: { $type: \"string\" }}},\n"
                + "                     {{Tipo: { $in: [\"especial\"] }}, {SiglaPartido: { $exists: false }}, {CPF: { $exists: false}}, {Idade: { $exists: false }}, {Apelido: { $exists: false }}}\n"
                + "                ]}\n"
                + "     ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE09CARGO\",\n"
                + "	validator: { \n"
                + "	$and: [\n"
                + "		{_id: { $type: \"int\" }},\n"
                + "		{PossuiVice: { $type: \"int\" }, { $in: [0, 1] }},\n"
                + "		{AnoBase: { $type: \"int\" }, { $gte: 1985 }, { $lte: 2100 }},\n"
                + "		{AnosMandato: { $type: \"int\" }, { $gte: 0 }},\n"
                + "		{NomeDescritivo: { $type: \"string\" }},\n"
                + "		{NroDeCadeiras: { $type: \"int\" }, { $gte: 0 }},\n"
                + "		{Esfera: { $type: \"string\" }, { $in: [\"F\", \"E\", \"M\"] }},\n"
                + "		{ $or: [ \n"
                + "			{{Esfera: { $in: [\"F\"] }}, {NomeCidade: { $exists: false }}, {SiglaEstado: { $exists: false }}},\n"
                + "			{{Esfera: { $in: [\"E\"] }}, {NomeCidade: { $exists: false }}, {SiglaEstado: { $type: \"string\" }}},\n"
                + "			{{Esfera: { $in: [\"M\"] }}, {NomeCidade: { $type: \"string\" }}, {SiglaEstado: { $type: \"string\" }}}\n"
                + "        	]}\n"
                + "       ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE10CANDIDATURA\",\n"
                + "	validator: { \n"
                + "	$and: [\n"
                + "		{_id: { $type: \"int\" }},\n"
                + "		{\"CodCargo._id\": { $type: \"int\" },\n"
                + "		{Ano: { $type: \"int\" }, { $gte: 1985 }, { $lte: 2100 }},\n"
                + "		{\"NroCand._id\": { $type: \"int\" }}\n"
                + "     ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE12PESQUISA\",\n"
                + "	validator: { \n"
                + "	$and: [\n"
                + "		{_id: { $type: \"int\" }},\n"
                + "		{PeriodoInicio: { $type: \"string\" }},\n"
                + "		{PeriodoFim: { $type: \"string\" }}\n"
                + "     ]\n"
                + "	}\n"
                + "})";
        /*String text2
                = "db.runCommand({\n"
                + "	collMod: \"LE02CIDADE\",\n"
                + "	validator: { \n"
                + "		$and: [\n"
                + "			{_id: { $type: \"string\" }},\n"
                + "			{nome: { $type: \"string\" }},\n"
                + "			{\"estado.Nome\": { $type: \"string\" }}, \n"
                + "			{\"estado._id\": { $type: \"string\" }}, \n"
                + "			{Populacao: { $type: \"int\" }, { $gte: 0 }}\n"
                + "        ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE03ZONA\",\n"
                + "	validator: { \n"
                + "		$and: [\n"
                + "			{_id: { $type: \"int\" }}, \n"
                + "			{NroDeUrnasReservas: { $type: \"int\" }}\n"
                + "        ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE04BAIRRO\",\n"
                + "	validator: { \n"
                + "		$and: [\n"
                + "			{_id: { $type: \"string\" }}, \n"
                + "			{\"Cidade._id\": { $type: \"string\" }}, \n"
                + "			{\"Zona._id\": { $type: \"int\" }}\n"
                + "        ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE05URNA\",\n"
                + "	validator: { \n"
                + "		$and: [\n"
                + "			{_id: { $type: \"int\" }}, \n"
                + "			{Estado: { $type: \"string\" }, { $in: [\"funcional\", \"manutencao\"] }}\n"
                + "        ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE06SESSAO\",\n"
                + "	validator: { \n"
                + "		$and: [\n"
                + "			{_id: { $type: \"int\" }},\n"
                + "			{\"NSerial._id\": { $type: \"int\" }}\n"
                + "        ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE07PARTIDO\",\n"
                + "	validator: { \n"
                + "		$and: [\n"
                + "			{_id: { $type: \"string\" }},\n"
                + "			{Nome: { $type: \"string\" }}\n"
                + "        ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE08CANDIDATO\",\n"
                + "	validator: { \n"
                + "		$and: [\n"
                + "			{_id: { $type: \"int\" }}, \n"
                + "			{Tipo: { $type: \"string\" }},\n"
                + "			{Nome: { $type: \"string\" }},\n"
                + "			{ $or: [\n"
                + "				{{Tipo: { $in: [\"politico\"] }}, {SiglaPartido: { $type: \"string\" }}, {CPF: { $type: \"string\" }}},\n"
                + "				{{Tipo: { $in: [\"especial\"] }}, {SiglaPartido: { $exists: false }}, {CPF: { $exists: false}}, {Idade: { $exists: false }}, {Apelido: { $exists: false }}}\n"
                + "        	]}\n"
                + "        ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE09CARGO\",\n"
                + "	validator: { \n"
                + "		$and: [\n"
                + "			{_id: { $type: \"int\" }},\n"
                + "			{PossuiVice: { $type: \"int\" }, { $in: [0, 1] }},\n"
                + "			{AnoBase: { $type: \"int\" }, { $gte: 1985 }, { $lte: 2100 }},\n"
                + "			{AnosMandato: { $type: \"int\" }, { $gte: 0 }},\n"
                + "			{NomeDescritivo: { $type: \"string\" }},\n"
                + "			{NroDeCadeiras: { $type: \"int\" }, { $gte: 0 }},\n"
                + "			{Esfera: { $type: \"string\" }, { $in: [\"F\", \"E\", \"M\"] }},\n"
                + "			{ $or: [ \n"
                + "				{{Esfera: { $in: [\"F\"] }}, {NomeCidade: { $exists: false }}, {SiglaEstado: { $exists: false }}},\n"
                + "				{{Esfera: { $in: [\"E\"] }}, {NomeCidade: { $exists: false }}, {SiglaEstado: { $type: \"string\" }}},\n"
                + "				{{Esfera: { $in: [\"M\"] }}, {NomeCidade: { $type: \"string\" }}, {SiglaEstado: { $type: \"string\" }}}\n"
                + "        	]}\n"
                + "        ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE10CANDIDATURA\",\n"
                + "	validator: { \n"
                + "		$and: [\n"
                + "			{_id: { $type: \"int\" }},\n"
                + "			{\"CodCargo._id\": { $type: \"int\" },\n"
                + "			{Ano: { $type: \"int\" }, { $gte: 1985 }, { $lte: 2100 }},\n"
                + "			{\"NroCand._id\": { $type: \"int\" }}\n"
                + "        ]\n"
                + "	}\n"
                + "})\n"
                + "\n"
                + "db.runCommand({\n"
                + "	collMod: \"LE12PESQUISA\",\n"
                + "	validator: { \n"
                + "		$and: [\n"
                + "			{_id: { $type: \"int\" }},\n"
                + "			{PeriodoInicio: { $type: \"string\" }},\n"
                + "			{PeriodoFim: { $type: \"string\" }}\n"
                + "        ]\n"
                + "	}\n"
                + "})";*/
        textCheck.setText(text);
        try {
            FileWriter arq = new FileWriter(new File("checks.txt"));
            PrintWriter gravarArq = new PrintWriter(arq);
            gravarArq.printf(text);
            arq.close();
            System.out.println("Criado em .txt!");
        } catch (Exception e) {
            System.out.println("impossivel escrever arq");
        }
    }

}
