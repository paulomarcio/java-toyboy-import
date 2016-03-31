package br.com.toyboy;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Paulo on 09/03/16.
 */
public class Integracao {

    protected String driverConnection;
    protected String urlConnection;
    protected String dataBaseUser;
    protected String dataBasePassword;
    protected String apiEndPoint;
    protected String apiUser;
    protected String apiPassword;
    protected Integer timeout;
    protected Connection connection;

    protected Retrofit retrofit;
    protected ApiToyboyInterface apiToyboyInterface;

    protected Usuario usuario;

    public static void main(String[] args){
        Integracao integracao = new Integracao();
        integracao.loadConfig();
        integracao.menu();
    }

    public void titulo(){
        System.out.println("================================================================");
        System.out.println("========= SISTEMA DE INTEGRAÇÃO E-COMMERCE - PDVNET ============");
        System.out.println("================================================================\n");
    }

    public void menu(){
        Scanner s = new Scanner(System.in);

        clearConsole();
        titulo();

        System.out.println("SELECIONE UMA OPÇÃO ABAIXO:\n");
        System.out.println("1 - ATUALIZAÇÃO PREÇO/ESTOQUE PDVNET -> E-COMMERCE");
        System.out.println("2 - FINALIZAR\n");
        System.out.print("OPÇÃO: ");

        while (!s.hasNextInt()){
            s.next();
            System.out.println("\nOPÇÃO INVÁLIDA!\n");
            menu();
        }

        int opcao = s.nextInt();

        switch(opcao){
            case 1:
                updatePrices();
                break;
            case 2:
                System.exit(0);
                break;
            default:
                System.out.println("\nOPÇÃO INVÁLIDA!\n");
                menu();
                break;
        }
    }

    public Produto loadProductFromPdv(Produto prodEcommerce){

        try {

            StringBuffer sql = new StringBuffer();

            /* Consulta usando join das tabelas */
            sql.append("SELECT R.REF_REFERENCIA, P.PRE_PRECO1, P.PRE_PRECO2, SUM(S.SAL_SALDO) AS ESTOQUE FROM PRECO P ");
            sql.append("JOIN MATERIAIS M ON M.MAT_CODIGO = P.PRE_PRODUTO ");
            sql.append("JOIN SALDOS S ON S.SAL_PRODUTO = M.MAT_CODIGO ");
            sql.append("JOIN REFERENCIAS R ON R.REF_REFERENCIA = M.MAT_REFERENCIA ");
            sql.append("JOIN FILIAL F ON F.FIL_CODIGO = S.SAL_FILIAL ");
            sql.append("WHERE P.PRE_TABELA = 13 AND F.FIL_CODIGO IN (1,3,5,11)");
            sql.append("AND R.REF_REFERENCIA = ? ");
            sql.append("GROUP BY R.REF_REFERENCIA, P.PRE_PRECO1, P.PRE_PRECO2");

            int i = 0;
            PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());
            preparedStatement.setString(++i, prodEcommerce.getReferencia());

            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()){

                String referencia = resultSet.getString("REF_REFERENCIA").trim();
                float valor1 = resultSet.getFloat("PRE_PRECO1");
                float valor2 = resultSet.getFloat("PRE_PRECO2");
                float preco = (valor2 == 0) ? valor1 : valor2;
                int quantidade = resultSet.getInt("ESTOQUE");

                prodEcommerce.setReferencia(referencia);
                prodEcommerce.setPreco(preco);
                prodEcommerce.setQuantidade(quantidade);

                resultSet.close();
                preparedStatement.close();

                return prodEcommerce;
            }
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        return prodEcommerce;
    }

    public void updatePrices(){
        dbConnect();

        java.util.Date d = GregorianCalendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat();
        System.out.println("\nOPERAÇÃO INICIADA EM: " + format.format(d));

        System.out.println("\nCARREGANDO PRODUTOS...");

        Call<Usuario> call = apiToyboyInterface.listaProdutos(usuario);

        try {

            Response<Usuario> response = call.execute();

            if(response.code() == 200){
                Boolean success = response.body().getSuccess();

                if(success){
                    List<Produto> produtos = response.body().getProdutos();

                    System.out.println("ATUALIZANDO INFORMAÇÕES DOS PRODUTOS CARREGADOS...");

                    for(int i = 0; i < produtos.size(); i++){
                        Produto pdv = loadProductFromPdv(produtos.get(i));

                        if(pdv.getPreco() > 0) {
                            produtos.set(i, pdv);
                            System.out.println("PRODUTO " + pdv.getReferencia() + " - " + pdv.getQuantidade() + " - " + pdv.getPreco() + " ATUALIZADO COM SUCESSO.");
                        }
                    }

                    usuario.setProdutos(produtos);

                    System.out.println("\nENVIANDO INFORMAÇÕES PARA O E-COMMERCE...\n");

                    Call<Usuario> userAdapter = apiToyboyInterface.atualizaProdutos(usuario);
                    Response<Usuario> userResponse = userAdapter.execute();

                    if(userResponse.code() == 200){
                        List<Mensagem> mensagens = userResponse.body().getMensagens();

                        for(Mensagem message: mensagens){
                            System.out.println(message.getMessage());
                        }
                    } else {
                        System.out.println("Falha na comunicação com a API: " + userResponse.errorBody().string());
                    }

                    d = GregorianCalendar.getInstance().getTime();
                    format = new SimpleDateFormat();

                    System.out.println("\nOPERAÇÃO FINALIZADA COM SUCESSO.");
                    System.out.println("OPERAÇÃO FINALIZADA EM: " + format.format(d) + "\n");

                    dbDisconnect();
                    menu();
                } else {
                    List<Mensagem> mensagens = response.body().getMensagens();

                    for(Mensagem message: mensagens){
                        System.out.println(message.getMessage());
                    }

                    dbDisconnect();
                    menu();
                }
            } else {
                System.out.println("Falha na comunicação com a API: " + response.errorBody().string());

                dbDisconnect();
                menu();
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());

            dbDisconnect();
            menu();
        }
    }

    public void dbConnect(){
        try {
            Class.forName(driverConnection);
            connection = DriverManager.getConnection(urlConnection, dataBaseUser, dataBasePassword);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public void dbDisconnect(){
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void loadConfig(){
        Properties properties = new Properties();

        try {

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            properties.load(classLoader.getResourceAsStream("config.properties"));

            // Carregando informações do arquivo de configurações
            driverConnection = properties.getProperty("driverConnection");
            urlConnection = properties.getProperty("connectionUrl");
            dataBaseUser = properties.getProperty("dataBaseUser");
            dataBasePassword = properties.getProperty("dataBasePassword");
            apiEndPoint = properties.getProperty("apiEndPoint");
            apiUser = properties.getProperty("apiUser");
            apiPassword = properties.getProperty("apiPassword");

            try{
                timeout = Integer.valueOf(properties.getProperty("timeout"));
            }catch(NumberFormatException e){
                System.out.println("O formato do timeou deve ser numérico.");
            }

            // Inicializando Retrofit para utilização da API da Toyboy
            retrofit = new Retrofit.Builder()
                    .baseUrl(apiEndPoint)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(new OkHttpClient().newBuilder().readTimeout(timeout, TimeUnit.SECONDS).writeTimeout(timeout, TimeUnit.SECONDS).build())
                    .build();

            // Instanciando interface para a API da Toyboy
            apiToyboyInterface = retrofit.create(ApiToyboyInterface.class);

            // Gerando o Usuario de acesso a API
            usuario = new Usuario();
            usuario.setUsuario(apiUser);
            usuario.setSenha(apiPassword);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    protected void clearConsole(){
        try {
            final String os = System.getProperty("os.name");
            if (os.contains("Windows")){
                Runtime.getRuntime().exec("cmd /c cls");
            } else {
                Runtime.getRuntime().exec("clear");
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

}
