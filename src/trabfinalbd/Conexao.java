/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabfinalbd;

import javax.swing.JPasswordField;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
/**
 *
 * @author Adriller Ferreira
 */
public class Conexao {

    JTextArea jtAreaDeStatus;
    Connection connection;
    Statement stmt;
    ResultSet rs;
    ArrayList<String> nomesTabelas = new ArrayList<>();

    Conexao(JTextArea AreaStatus) {
        jtAreaDeStatus = AreaStatus;
    }

    ActionListener Conectar(final JTextField esq, final JPasswordField sen) {
        ActionListener temp = null;
        String esquema = esq.getText();
        String senha = String.valueOf(sen.getPassword());
        try {
            System.out.println("Entrando em:");
            System.out.println(esquema);
            System.out.println(senha);
            Class.forName("oracle.jdbc.OracleDriver");
            connection = DriverManager.getConnection(
                    "jdbc:oracle:thin:@grad.icmc.usp.br:15215:orcl",
                    esquema,
                    senha);
            jtAreaDeStatus.setText("Conectado por padrao no usuario a8922201");
            System.out.println("Conectado com Sucesso");
        } catch (ClassNotFoundException ex) {
            jtAreaDeStatus.setText("Problema: verifique o driver do banco de dados");
            System.out.println("erro - conexao");
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Problema: verifique seu usuário e senha");
            System.out.println("erro2 - conexao");
        }
        temp = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                boolean bNext;
                String esquema = esq.getText();
                String senha = String.valueOf(sen.getPassword());
                System.out.println("tentando conectar ao usuario " + esquema);
                try {
                    //System.out.println(esquema);
                    //System.out.println(senha);
                   Class.forName("oracle.jdbc.OracleDriver");
                    connection = DriverManager.getConnection(
                            "jdbc:oracle:thin:@grad.icmc.usp.br:15215:orcl",
                            esquema,
                            senha);
                    jtAreaDeStatus.setText("Conectado com Sucesso ao " + esquema + "!");
                    jtAreaDeStatus.setBackground(Color.GREEN);
                } catch (ClassNotFoundException ex) {
                    jtAreaDeStatus.setText("Problema: verifique o driver do banco de dados");
                    jtAreaDeStatus.setBackground(Color.red);
                }catch (SQLException ex) {
                    jtAreaDeStatus.setText("Problema: verifique seu usuário e senha");
                    jtAreaDeStatus.setBackground(Color.red);
                }
            }
        };
        return temp;

    }

    public ArrayList pegarNomesDeTabelas(JComboBox jc) {
        String s = "";
        ArrayList<String> retorno = new ArrayList<>();
        try {
            s = "SELECT table_name FROM user_tables where table_name NOT IN (SeLECT table_name FROM user_snapshots)";
            stmt = connection.createStatement();
            rs = stmt.executeQuery(s);
            while (rs.next()) {
                jc.addItem(rs.getString("table_name"));
                retorno.add(rs.getString("table_name"));
                nomesTabelas.add(rs.getString("table_name"));
            }
            stmt.close();
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro na consulta: \"" + s + "\"");
        }
        return retorno;
    }

    void pegarNomesDeView(JComboBox jc) {
        String s = "";
        try {
            s = "SELECT view_name FROM user_views";
            stmt = connection.createStatement();
            rs = stmt.executeQuery(s);
            while (rs.next()) {
                jc.addItem(rs.getString("view_name") + " (view)");
                nomesTabelas.add(rs.getString("view_name"));
            }
            stmt.close();
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro na consulta: \"" + s + "\"");
        }
    }

    void pegarNomesDeMView(JComboBox jc) {
        String s = "";
        try {
            s = "SELECT table_name FROM user_snapshots";
            stmt = connection.createStatement();
            rs = stmt.executeQuery(s);
            while (rs.next()) {
                jc.addItem(rs.getString("table_name") + " (materialized view)");
                nomesTabelas.add(rs.getString("table_name"));
            }
            stmt.close();
        } catch (SQLException ex) {
            jtAreaDeStatus.setText("Erro na consulta: \"" + s + "\"");
        }
    }
}
