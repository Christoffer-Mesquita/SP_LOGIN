package org.zerolegion.sp_login;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bukkit.plugin.java.JavaPlugin;
import org.zerolegion.sp_login.commands.AuthAdminCommand;
import org.zerolegion.sp_login.commands.LoginCommand;
import org.zerolegion.sp_login.commands.RegisterCommand;
import org.zerolegion.sp_login.commands.SessionCommand;
import org.zerolegion.sp_login.listeners.AuthListener;
import org.zerolegion.sp_login.manager.AuthManager;
import org.zerolegion.sp_login.manager.LocationManager;
import org.zerolegion.sp_login.manager.LogManager;
import org.zerolegion.sp_login.session.SessionManager;
import org.zerolegion.sp_login.utils.MessageUtils;

public final class SP_LOGIN extends JavaPlugin {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private static SP_LOGIN instance;
    private AuthManager authManager;
    private SessionManager sessionManager;
    private LogManager logManager;
    private LocationManager locationManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Salvar configuração padrão se não existir
        saveDefaultConfig();
        
        // Inicializar conexão com MongoDB
        try {
            String mongoUri = getConfig().getString("mongodb-uri");
            if (mongoUri == null || mongoUri.isEmpty()) {
                getLogger().severe("URI do MongoDB não configurada!");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            
            mongoClient = MongoClients.create(mongoUri);
            database = mongoClient.getDatabase("auth"); // Nome fixo do banco de dados

            // Inicializar gerenciadores
            authManager = new AuthManager(this);
            sessionManager = new SessionManager(this);
            logManager = new LogManager(this);
            locationManager = new LocationManager(this);

            // Registrar comandos
            getCommand("login").setExecutor(new LoginCommand(this));
            getCommand("register").setExecutor(new RegisterCommand(this));
            getCommand("session").setExecutor(new SessionCommand(this));
            getCommand("auth").setExecutor(new AuthAdminCommand(this));

            // Registrar eventos
            getServer().getPluginManager().registerEvents(new AuthListener(this), this);

            // Exibir mensagem de inicialização
            org.zerolegion.sp_login.utils.LogManager.logStartup(this);
        } catch (Exception e) {
            getLogger().severe("Erro ao inicializar o plugin: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    @Override
    public void onDisable() {
        if (mongoClient != null) {
            mongoClient.close();
        }
        org.zerolegion.sp_login.utils.LogManager.logShutdown(this);
    }

    public static SP_LOGIN getInstance() {
        return instance;
    }

    public MongoDatabase getMongoDB() {
        return database;
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }
}
