/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabfinalbd;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author Adriller Ferreira
 */
class JanelaPrincipal {
    
    JFrame j;
    JPanel pPainelDeCima;
    JPanel pPainelDeBaixo;
    JComboBox jc;
    JTextArea jtAreaDeStatus;
    JTabbedPane tabbedPane;
    JButton Conectar;
    String tabelaAtual = "a";
    ex1 e1;
    JPanel JPex1;
    JTextArea tfGerador;
    JScrollPane scrollPane;
    JButton gerar;
    JPanel PainelCentro;
    JPanel JPex2;
    JPanel JPex3;
    JPanel JPex4;
    ArrayList<String> nomesTabelas = new ArrayList<>();
    ArrayList<String> naoPossuiEmbed = new ArrayList<>();
    
    ex4 e4 = new ex4();
    
    public void ExibeJanelaPrincipal() {
        /*Cria a Janela*/
        j = new JFrame("ICMC-USP - SCC0241 - Trab Final - AdrillerFerreira - HikaroAugusto");
        j.setSize(700, 500);
        j.setLayout(new BorderLayout());
        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*Painel da parte superior (north) - com combobox e outras informações*/
        pPainelDeCima = new JPanel();
        j.add(pPainelDeCima, BorderLayout.NORTH);
        jc = new JComboBox();
        pPainelDeCima.add(jc);

        /*Painel da parte inferior (south) - com área de status*/
        pPainelDeBaixo = new JPanel();
        j.add(pPainelDeBaixo, BorderLayout.SOUTH);
        jtAreaDeStatus = new JTextArea();
        jtAreaDeStatus.setText("Aqui é sua área de status");
        pPainelDeBaixo.add(jtAreaDeStatus);

        /*Painel tabulado na parte central (CENTER)*/
        tabbedPane = new JTabbedPane();
        j.add(tabbedPane, BorderLayout.CENTER);

        /*Tela de Login*/
        JPanel inicial = new JPanel();
        inicial.setLayout(new GridLayout(3, 2));
        JLabel lEsquema = new JLabel("Digite o nome do Esquema");
        inicial.add(lEsquema);
        
        JTextField esquema = new JTextField();
        esquema.setText("a8922201");
        inicial.add(esquema);
        
        JLabel lSenha = new JLabel("Digite sua senha");
        inicial.add(lSenha);
        
        JPasswordField senha = new JPasswordField();
        senha.setText("fiskyou2");
        inicial.add(senha);
        
        Conectar = new JButton("Conectar");
        inicial.add(Conectar);
        tabbedPane.add(inicial, "Criar Conexao");
        
        Conexao conexao = new Conexao(jtAreaDeStatus);
        Conectar.addActionListener(conexao.Conectar(esquema, senha));

        /*Preencher ComboBox*/
        nomesTabelas = conexao.pegarNomesDeTabelas(jc);

        //conexao.pegarNomesDeView(jc);
        //conexao.pegarNomesDeMView(jc);
        e1 = new ex1(esquema, senha, nomesTabelas);
        
        for (int i = 0; i < nomesTabelas.size(); i++) {
            if (e1.pegarReferences(nomesTabelas.get(i)).isEmpty()) {
                naoPossuiEmbed.add(nomesTabelas.get(i));
            }
        }
        
        j.setVisible(true);
        
        this.DefineEventosJC();
        gerar = new JButton("Script da tabela atual");
    }
    
    private void DefineEventosJC() {
        jc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                /*Pega a tabela selecionada*/
                JComboBox jcTemp = (JComboBox) e.getSource();
                jtAreaDeStatus.setText((String) jcTemp.getSelectedItem());
                jtAreaDeStatus.setBackground(Color.CYAN);
                tabelaAtual = (String) jcTemp.getSelectedItem();
                System.out.println("tabela atual:" + tabelaAtual);

                /*Exercicio 1*/
                JPex1 = new JPanel();
                JPex1.setLayout(new BorderLayout());
                
                /*Parte onde ficara um checkbox sobre embeeding e botao para gerar Doc BSON*/
                JPanel PainelNorte = new JPanel();
                JCheckBox embeeding;
                embeeding = new JCheckBox("Embeeding");
                JButton criarScript = new JButton("Gerar todos scripts em .txt");
                if (tabelaAtual.equals("LE02CIDADE")) {
                    PainelNorte.add(embeeding);
                }
                PainelNorte.add(gerar);
                PainelNorte.add(criarScript);
                JPex1.add(PainelNorte, BorderLayout.NORTH);

                /*Parte onde ficara o codigo*/
                //PainelCentro = new JPanel();
                tfGerador = new JTextArea();
                scrollPane = new JScrollPane(tfGerador);
                //PainelCentro.add(scrollPane);
                JPex1.add(scrollPane, BorderLayout.CENTER);
                //System.out.println("gerado");
                gerar.addActionListener(e1.GerarBSON(tfGerador, tabelaAtual, JPex1, embeeding, naoPossuiEmbed));
                criarScript.addActionListener(e1.CriarScript(criarScript, JPex1, embeeding, naoPossuiEmbed));
                
                /*Exercicio 2*/
                JPex2 = new JPanel();
                JPanel PainelNorte2 = new JPanel();
                JButton criarIndices = new JButton("Gerar Indices");
                JTextArea textIndices = new JTextArea();
                JScrollPane scrollIndices = new JScrollPane(textIndices);   
                JPex2.setLayout(new BorderLayout());
                PainelNorte2.add(criarIndices);
                JPex2.add(PainelNorte2, BorderLayout.NORTH);
                JPex2.add(scrollIndices);
                criarIndices.addActionListener(e1.montaIndex(tabelaAtual, textIndices));
                
                /*Exercicio 3*/
                //JPex3 = new JPanel();
                JTextArea textCheck = new JTextArea();
                JScrollPane scrollCheck = new JScrollPane(textCheck);
                e1.EscreverChecks(textCheck);
                
                /*Exercicio 4*/
                JPex4 = new JPanel();
                JPex4.setLayout(new BorderLayout());
                JPanel painelNorte4 = new JPanel();
                
                JTextArea query = new JTextArea(2, 50);
                JButton pesquisar = new JButton("Pesquisar");
                painelNorte4.add(query);
                painelNorte4.add(pesquisar);
                JPex4.add(painelNorte4, BorderLayout.NORTH);
                
                JTextArea result = new JTextArea();
                pesquisar.addActionListener(e4.Pesquisar(tabelaAtual, result, query));
                result.setEditable(false);
                JScrollPane scrollPane4 = new JScrollPane(result);
                JPex4.add(scrollPane4, BorderLayout.CENTER);
                
                
                
                tabbedPane.removeAll();
                tabbedPane.add(JPex1, "ex1");
                tabbedPane.add(JPex2, "ex2");
                tabbedPane.add(scrollCheck, "ex3");
                tabbedPane.add(JPex4, "ex4");
                
            }
            
        });
        
    }
    
}
