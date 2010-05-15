package vues;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import models.jeu.Jeu;
import models.joueurs.Equipe;
import models.joueurs.Joueur;
import models.outils.Astuces;
import models.outils.GestionnaireSons;
import models.outils.MeilleursScores;
import models.outils.Outils;
import models.outils.Score;
import models.terrains.*;

/**
 * Fenetre du menu principal du jeu.
 * <p>
 * Affiche un menu permettant au joueur de choisir 
 * sur quel terrain il veut jouer.
 * <p>
 * Les boutons des terrains ont ete fait completement statiques 
 * pour gagner un temps precieux.
 * 
 * @author Pierre-Dominique Putallaz
 * @author Aurelien Da Campo
 * @author Lazhar Farjallah
 * @version 1.0 | 15 decembre 2009
 * @since jdk1.6.0_16
 */
public class Panel_ModeSolo extends JPanel implements ActionListener, Runnable
{
	// constantes statiques
    private static final long serialVersionUID 	= 1L;
	private static final ImageIcon I_QUITTER 	= new ImageIcon("img/icones/door_out.png");
	private static final ImageIcon I_AIDE 		= new ImageIcon("img/icones/help.png");
	private static final ImageIcon I_SCORE      = new ImageIcon("img/icones/star.png");
	private static final int IMAGE_MENU_LARGEUR = 120;
	private static final int IMAGE_MENU_HAUTEUR = 120;
	private static final ImageIcon icoCADENAS      = new ImageIcon("img/icones/lock.png");

    
	private final int MARGES_PANEL                 = 40;
    
	// elements du formulaire
	private final JMenuBar 	menuPrincipal 		= new JMenuBar();
	private final JMenu 	menuFichier 		= new JMenu("Fichier");
	private final JMenu     menuMeilleursScore  = new JMenu("Scores");
	private final JMenu 	menuAide 			= new JMenu("Aide");
	private final JMenuItem itemAPropos	    	= new JMenuItem("A propos",I_AIDE);
	private final JMenuItem itemQuitter	   		= new JMenuItem("Quitter",I_QUITTER);
	
	private final JMenuItem itemMSElementTD     = new JMenuItem(ElementTD.NOM);
    private final JMenuItem itemMSSpiral        = new JMenuItem(Spiral.NOM);
    private final JMenuItem itemMSDesert        = new JMenuItem(Desert.NOM);
    private final JMenuItem itemMSWaterWorld    = new JMenuItem(WaterWorld.NOM);
	
	private final JButton[] boutonsTerrains     = new JButton[4]; 
	private final JButton[] boutonsScore        = new JButton[4]; 
	private final JButton bRetour              = new JButton("Retour");
	
	private JProgressBar chargementTerrain;
	private Thread thread;
    private boolean chargementTermine;
	private JFrame parent;
	
	private JLabel lblInfo = new JLabel("Cliquez sur un terrain débloqué pour commencer une partie.");
	
	
	/**
	 * Constructeur de la fenetre du menu principal
	 */
	public Panel_ModeSolo(JFrame parent)
	{
		//-------------------------------
		//-- preferances de le fenetre --
		//-------------------------------
	    this.parent = parent;
	    setLayout(new BorderLayout());
		parent.setTitle("Mode Solo - ASD Tower Defense");

		setBorder(new EmptyBorder(new Insets(MARGES_PANEL, MARGES_PANEL,
                MARGES_PANEL, MARGES_PANEL)));
		
		setBackground(LookInterface.COULEUR_DE_FOND);
		
		
		//--------------------
		//-- menu principal --
		//--------------------
		// menu Fichier
		menuMeilleursScore.setIcon(I_SCORE);
		menuFichier.add(menuMeilleursScore);
		menuMeilleursScore.add(itemMSElementTD);
		menuMeilleursScore.add(itemMSSpiral);
		menuMeilleursScore.add(itemMSDesert);
		menuMeilleursScore.add(itemMSWaterWorld);
		
		itemMSElementTD.addActionListener(this);
		itemMSSpiral.addActionListener(this);
		itemMSDesert.addActionListener(this);
		itemMSWaterWorld.addActionListener(this);
		
		menuFichier.add(itemQuitter);
		menuPrincipal.add(menuFichier);
		
		// menu A propos
		menuAide.add(itemAPropos);
		menuPrincipal.add(menuAide);
		
		// ajout des ecouteurs
		itemQuitter.addActionListener(this);
		itemAPropos.addActionListener(this);
		
		// ajout du menu
		//parent.setJMenuBar(menuPrincipal);
		
		
		//--------------------------------------
        //-- chargement des scores et étoiles --
        //--------------------------------------
		
	    String[] nomTerrains = new String[]{"ElementTD","Spiral","Desert","WaterWorld"};
        Score[] scoresMax    = new Score[4];
        MeilleursScores ms;
        int nbEtoiles = 0;
        
        for(int i=0; i < nomTerrains.length; i++)
        {  
            ms = new MeilleursScores("donnees/"+nomTerrains[i]+".ms");
            
            if(ms.getScores().size() > 0)
            {
                Score score = ms.getScores().get(0); 
                nbEtoiles += score.getNbEtoiles();
                scoresMax[i] = score;
            }
            else
                scoresMax[i] = new Score(" ",0,0);
        }
		
		
		//----------------------------
        //-- création du formulaire --
        //----------------------------
		
		JPanel pFormulaire = new JPanel(new BorderLayout());
		pFormulaire.setOpaque(false);
		
		
		//------------------------------
        //-- titre + nombre d'étoiles --
        //------------------------------
		JPanel pNord = new JPanel(new BorderLayout());
		pNord.setOpaque(false);
		
		// titre
		JLabel lblTitre = new JLabel("PARTIE SOLO");
		lblTitre.setFont(GestionnaireDesPolices.POLICE_TITRE);
		lblTitre.setForeground(GestionnaireDesPolices.COULEUR_TITRE);
		pNord.add(lblTitre,BorderLayout.WEST);
		
		// étoiles
		JPanel pNbEtoiles = new JPanel(new FlowLayout());
		pNbEtoiles.setOpaque(false);
		
		JLabel lblNbEtoiles = new JLabel(nbEtoiles+" x");
		lblNbEtoiles.setFont(GestionnaireDesPolices.POLICE_SOUS_TITRE);
		lblNbEtoiles.setForeground(GestionnaireDesPolices.COULEUR_TXT_SUR_COULEUR_DE_FOND);
		
		pNbEtoiles.add(lblNbEtoiles);
		pNbEtoiles.add(new JLabel(I_SCORE));
		pNord.add(pNbEtoiles,BorderLayout.EAST);
		
        pFormulaire.add(pNord,BorderLayout.NORTH);
		
		
		//-----------------------------
		//-- chargement des terrains --
		//-----------------------------
		
		// attent que toutes les images soit complementements chargees
		MediaTracker tracker = new MediaTracker(this);
		tracker.addImage(ElementTD.IMAGE_MENU, 0);
		tracker.addImage(Spiral.IMAGE_MENU, 1);
		tracker.addImage(Desert.IMAGE_MENU, 2);
		tracker.addImage(WaterWorld.IMAGE_MENU, 3);
		
		try { 
			tracker.waitForAll(); 
		} 
		catch (InterruptedException e){ 
			e.printStackTrace(); 
		}
		
		// creation des boutons
		boutonsTerrains[0] = new JButton(new ImageIcon(
							Outils.redimentionner(ElementTD.IMAGE_MENU,
									IMAGE_MENU_LARGEUR,IMAGE_MENU_HAUTEUR)));
		
		boutonsTerrains[1] = new JButton(new ImageIcon(
							Outils.redimentionner(Spiral.IMAGE_MENU,
									IMAGE_MENU_LARGEUR,IMAGE_MENU_HAUTEUR)));
		
		boutonsTerrains[2] = new JButton(new ImageIcon(
							Outils.redimentionner(Desert.IMAGE_MENU,
									IMAGE_MENU_LARGEUR,IMAGE_MENU_HAUTEUR)));
		
		boutonsTerrains[3] = new JButton(new ImageIcon(
							Outils.redimentionner(WaterWorld.IMAGE_MENU,
									IMAGE_MENU_LARGEUR,IMAGE_MENU_HAUTEUR)));

		// ajout des boutons au panel et ajout des ecouteurs
		JPanel pBoutonsTerrains = new JPanel(new FlowLayout());
		
		pBoutonsTerrains.setBorder(new EmptyBorder(new Insets(60, 0, 0, 0)));
		
		
		pBoutonsTerrains.setOpaque(false);
		
		for(int i=0; i < boutonsTerrains.length; i++)
		{
		    JButton bouton = boutonsTerrains[i];
		    GestionnaireDesPolices.setStyle(bouton);
		    
		    
		    JPanel pInfoTerrain = new JPanel(new BorderLayout());
		    pInfoTerrain.setOpaque(false);
		    
		    
		    bouton.addActionListener(this);
		    pInfoTerrain.add(bouton,BorderLayout.NORTH);
		    
		    // recuperation du meilleur score
		    Score score = scoresMax[i];
		    
		    pInfoTerrain.add(new Panel_Etoiles(score),BorderLayout.CENTER);
		    
		    
		    if(score.getValeur() > 0)
		    {
		        String txt = score.getNomJoueur()+" - "+score.getValeur()+"";
		    
		        JButton bScore = new JButton(txt);
		    
        	    boutonsScore[i] = bScore;
        	    bScore.setBackground(LookInterface.COULEUR_BOUTON);
        	    bScore.addActionListener(this);
        	    
        	    //bScore.setFont(GestionnaireDesPolices.POLICE_SOUS_TITRE);
        	    bScore.setForeground(GestionnaireDesPolices.COULEUR_SOUS_TITRE);

        	    pInfoTerrain.add(bScore,BorderLayout.SOUTH);
		    }
		    else
		    {
		        JPanel tmp = new JPanel();
		        tmp.setPreferredSize(new Dimension(1,30));
		        tmp.setOpaque(false);
		        pInfoTerrain.add(tmp,BorderLayout.SOUTH);
		    }
		    //-----------------------------------------
	        //-- bloquage des terrains - progression --
	        //-----------------------------------------
		    
		    if(i == 1 && nbEtoiles < 1)
		    {
		        bouton.setEnabled(false);
		        
		        pInfoTerrain.add(new JLabel("1 étoile min.",icoCADENAS,0),BorderLayout.SOUTH);
		    }
		        
		    if(i == 2 && nbEtoiles < 3)
		    {
                bouton.setEnabled(false);
		        pInfoTerrain.add(new JLabel("3 étoiles min.",icoCADENAS,0),BorderLayout.SOUTH);
		    }
		    
		    if(i == 3 && nbEtoiles < 7)
		    {
		        bouton.setEnabled(false);
		        pInfoTerrain.add(new JLabel("7 étoiles min.",icoCADENAS,0),BorderLayout.SOUTH);
		    }

		    // ajout au panel
		    pBoutonsTerrains.add(pInfoTerrain);
		}
	
		
		
		JPanel pCentre = new JPanel(new BorderLayout());
		pCentre.setOpaque(false);
		
		pCentre.add(pBoutonsTerrains,BorderLayout.NORTH);
		
		JLabel lblAstuce = new JLabel("\""+Astuces.getAstuceAleatoirement()+"\"");
		lblAstuce.setFont(GestionnaireDesPolices.POLICE_SOUS_TITRE);
		lblAstuce.setForeground(GestionnaireDesPolices.COULEUR_TXT_SUR_COULEUR_DE_FOND);
		pCentre.add(lblAstuce,BorderLayout.EAST);
        
		pFormulaire.add(pCentre,BorderLayout.CENTER);
		
		
		
	    //-------------------
        //-- bouton retour --
        //-------------------
		
		JPanel pFond = new JPanel(new BorderLayout());
		
		
		
		
		pFond.setOpaque(false);
		pFond.setBorder(new EmptyBorder(0, 0, 0, 100));
		
		
		bRetour.addActionListener(this);
		bRetour.setPreferredSize(new Dimension(80,60));
		GestionnaireDesPolices.setStyle(bRetour);
		pFond.add(bRetour,BorderLayout.WEST);
        pFormulaire.add(pFond,BorderLayout.SOUTH);


        lblInfo.setFont(GestionnaireDesPolices.POLICE_INFO);
        //lblInfo.setForeground(GestionnaireDesPolices.COULEUR_INFO);
        lblInfo.setForeground(GestionnaireDesPolices.COULEUR_TXT_SUR_COULEUR_DE_FOND);
        
        pFond.add(lblInfo,BorderLayout.EAST);
        
		add(pFormulaire,BorderLayout.CENTER);
	}

    /**
     * Gestionnaire des evenements. 
     * <p>
     * Cette methode est appelee en cas d'evenement
     * sur un objet ecouteur de ActionListener
     * 
     * @param ae l'evenement associe
     */
	public void actionPerformed(ActionEvent ae)
	{
		Object source = ae.getSource();
		
		// quitter
		if(source == itemQuitter)
			System.exit(0); // Fermeture correcte du logiciel
		
		// a propos
		else if(source == itemAPropos)
		    new Fenetre_HTML("A propos", new File("aPropos/aPropos.html"),parent);
		
		// les terrains
		else if(source == boutonsTerrains[0])
		{
		    Jeu jeu = new Jeu();
		    lancerJeu(jeu, new ElementTD(jeu));
		}
		else if(source == boutonsTerrains[1])
		{
		    Jeu jeu = new Jeu();
            lancerJeu(jeu, new Spiral(jeu));
		}
		else if(source == boutonsTerrains[2])
		{ 
		    Jeu jeu = new Jeu();
		    lancerJeu(jeu, new Desert(jeu));
		}
		else if(source == boutonsTerrains[3])
		{
		    Jeu jeu = new Jeu();
		    lancerJeu(jeu, new WaterWorld(jeu));
		}
		else if(source == itemMSElementTD)
		    new Fenetre_MeilleursScores(ElementTD.NOM, parent);
		else if(source == itemMSSpiral)
            new Fenetre_MeilleursScores(Spiral.NOM, parent);
		else if(source == itemMSDesert)
            new Fenetre_MeilleursScores(Desert.NOM, parent);
		else if(source == itemMSWaterWorld)
            new Fenetre_MeilleursScores(WaterWorld.NOM, parent);
		else if(source == bRetour)
		{
		    parent.getContentPane().removeAll();
            parent.getContentPane().add(new Panel_MenuPrincipal(parent),
                    BorderLayout.CENTER);
            parent.getContentPane().validate();
		}
		else if(source == boutonsScore[0])
            new Fenetre_MeilleursScores("ElementTD", parent);    
        else if(source == boutonsScore[1])
            new Fenetre_MeilleursScores("Spiral", parent); 
        else if(source == boutonsScore[2])
            new Fenetre_MeilleursScores("Desert", parent); 
        else if(source == boutonsScore[3])
            new Fenetre_MeilleursScores("WaterWorld", parent);   
	}

	/**
	 * Permet de lancer un jeu.
	 * 
	 * Elle créer les équipes pour un jeu solo
	 * 
	 * @param jeu le jeu
	 * @param terrain le terrain
	 */
    private void lancerJeu(Jeu jeu, Terrain terrain)
    {
        GestionnaireSons.arreterTousLesSons(Fenetre_MenuPrincipal.FICHIER_MUSIQUE_MENU);
        
        actionnerBarreDeChargement();
        
        jeu.setTerrain(terrain);
        Equipe equipe = jeu.getEquipes().get(0); // les equipes sont créer par le terrain
        Joueur joueur = new Joueur("Joueur");
        equipe.ajouterJoueur(joueur);
        jeu.initialiser();
        new Fenetre_JeuSolo(jeu,joueur);
        
        chargementTermine = true;
        parent.dispose();
    }

    synchronized private void actionnerBarreDeChargement()
    {  
        thread = new Thread(this);
        thread.start();   
    }

    @Override
    public void run()
    {
       /* version.setText("   CHARGEMENT DE LA CARTE");
        version.setForeground(Color.BLACK);*/
        chargementTerrain = new JProgressBar();
        add(chargementTerrain,BorderLayout.SOUTH);
        validate();
        
        int pourcent = 0;
        
        while(!chargementTermine)
        {
            pourcent = (pourcent+2)%100;
     
            chargementTerrain.setValue(pourcent);
            // TODO EXCEPTION ICI java.lang.ClassCastException
            //chargementTerrain.paintImmediately(0,0,1000,200);
            
            try{
                Thread.sleep(10);
            } 
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}
