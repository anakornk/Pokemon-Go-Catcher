import java.io.FileInputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.pokegoapi.api.PokemonGo;
import com.pokegoapi.api.map.MapObjects;
import com.pokegoapi.api.map.fort.Pokestop;
import com.pokegoapi.api.map.fort.PokestopLootResult;
import com.pokegoapi.api.map.pokemon.CatchResult;
import com.pokegoapi.api.map.pokemon.CatchablePokemon;
import com.pokegoapi.api.map.pokemon.encounter.EncounterResult;
import com.pokegoapi.auth.GoogleUserCredentialProvider;
import com.pokegoapi.auth.PtcCredentialProvider;

import POGOProtos.Inventory.Item.ItemAwardOuterClass.ItemAward;
import okhttp3.OkHttpClient;



public class PokemonCatcher{
	
	private static Properties prop= new Properties();;
	
	private static String user;
	private static String pass;
	private static String googleorptc;
	private static String token;
	private static double latitude;
	private static double longitude;

	
	private static Scanner sc = new Scanner(System.in);
	private static PokemonGo go;


	

	
	public static void main(String[] arg){
		System.out.println("Locating config.properties");		
		try{
			
			prop.load(new FileInputStream("config.properties"));
			user = prop.getProperty("USERNAME");
			pass = prop.getProperty("PASSWORD");
			googleorptc = prop.getProperty("GOOGLEORPTC");
			token = prop.getProperty("TOKEN","");
			latitude = Double.parseDouble(prop.getProperty("LATITUDE"));
			longitude = Double.parseDouble(prop.getProperty("LONGITUDE"));
			System.out.println("Loaded config.properties");			
		}
		catch(Exception e){
			System.out.println("Error: Cannot find config.properties\nExiting");
			return;
		}
		
		
		
		
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            	sc.close();
            	System.out.println("-----------------Exiting:please wait for 5 minutes----------------");
            	try{
            		sleep(5000);
            	}
            	catch(InterruptedException e){
            		
            	}
            	
            	go.setLocation(latitude, longitude,0);
            	System.out.println("Warped back to:" +  go.getLatitude() + ","+ go.getLongitude());           	
            	System.out.println("Goodbye!!");
            }
        });
		
		
		OkHttpClient http = new OkHttpClient();
		new Thread(){
		public void run(){
			//Login
			try {
				if(googleorptc.equals("GOOGLE")){
					if(token.equals("")){
					
						GoogleUserCredentialProvider provider = new GoogleUserCredentialProvider(http);

						// in this url, you will get a code for the google account that is logged
						System.out.println("Please go to: " + GoogleUserCredentialProvider.LOGIN_URL);
						System.out.println("Enter authorization code:");

						// Ask the user to enter the token in the standard input
						String authCode = sc.nextLine();

						// we should be able to login with this token
						provider.login(authCode);
						 go = new PokemonGo(provider, http);
						 System.out.println("Please edit the value of the 'TOKEN' field in config.properties");
						 System.out.println("Your token:" + provider.getRefreshToken());
						 
					}else{
						 go = new PokemonGo(new GoogleUserCredentialProvider(http, token), http);
					}


				}else{
					go = new PokemonGo(new PtcCredentialProvider(http,user,pass), http);
				}
				System.out.println("------------------------------------------------------------------");
				System.out.println("Hello "+go.getPlayerProfile().getPlayerData().getUsername() + ", Your level: "+ go.getPlayerProfile().getStats().getLevel());				 
				// Set to current location
				go.setLocation(latitude, longitude,0);
				System.out.println("Your current location: " +  go.getLatitude() + ","+ go.getLongitude());
				System.out.println("--------------------------Waiting: 5 sec--------------------------");
	        	sleep(5000);
			}
			catch(Exception e){
				System.out.println("Login error: "+e);
				return;
			}
			try{
				//Main Loop
				while(true){
					System.out.println("------------------------------------------------------------------");
					System.out.println("Catch pokemons:");
					List<CatchablePokemon> cpList = go.getMap().getCatchablePokemon();
					for(CatchablePokemon cp: cpList){
						EncounterResult encResult = cp.encounterPokemon();
						if(encResult.wasSuccessful()){
							CatchResult result = cp.catchPokemon();
							System.out.println("Catch Status: " + result.getStatus());
							//output info
							System.out.println("Pokemon Information:\nPokemon:" + encResult.getPokemonData().getPokemonId().toString());
							System.out.println("CP:" + encResult.getPokemonData().getCp());
							System.out.println("Height:" + encResult.getPokemonData().getHeightM() +"m");
							System.out.println("Weight:" + encResult.getPokemonData().getWeightKg()+"kg");
							System.out.println("MOVE 1:" + encResult.getPokemonData().getMove1());
							System.out.println("MOVE 2:" + encResult.getPokemonData().getMove2());
							int ivAttack = encResult.getPokemonData().getIndividualAttack();
							int ivDefense = encResult.getPokemonData().getIndividualDefense();
							int ivStamina = encResult.getPokemonData().getIndividualStamina();	
							System.out.println("Individual Attack: " + ivAttack);
							System.out.println("Individual Defense: " + ivDefense);
							System.out.println("Individual Stamina: " + ivStamina);
							System.out.println("IV Perfection:" +(ivAttack + ivDefense + ivStamina)*100/45.0 + "%");
						}
					}
					System.out.println("------------------------------------------------------------------");
					System.out.println("Loot pokestops:");
					MapObjects mo = go.getMap().getMapObjects();
					Collection<Pokestop> ps = mo.getPokestops();
					for(Pokestop p:ps){
					
						if(p.canLoot()){
							PokestopLootResult res = p.loot();
							if(res.wasSuccessful()){
								List<ItemAward> iaList = res.getItemsAwarded();
								for(ItemAward ia:iaList){
									System.out.println(ia.getItemId() + ":" + ia.getItemCount() );
								}
								
							}else{
								System.out.println("loot failed");
							}
						}
					}
					System.out.println("------------------------------------------------------------------");
					System.out.println("Sleep 60 seconds");
					
					sleep(60000+(int)Math.random()*30000);
					
					
				
					
					
			        
				}
			}
			catch(Exception e){
				System.out.println(e);
			}
			
			
			
		}
			
		}.start();
	
        
        
        
        

		
	}



}