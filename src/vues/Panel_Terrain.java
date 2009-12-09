package vues;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;
import models.creatures.Creature;
import models.jeu.Jeu;
import models.tours.Tour;

/**
 * Panel d'affichage du terrain de jeu.
 * 
 * Ce panel affiche la zone de jeu en elle-même.
 * Celle-ci affichera les tours avec les créatures et gèrera le positionnement
 * des tours et la selection des tours.
 * 
 * @author Pierre-Dominique Putallaz
 * @author Aurélien Da Campo
 * @author Lazhar Farjallah
 * @version 1.0 | 27 novemenbre 2009
 * @since jdk1.6.0_16
 * @see JPanel
 * @see Runnable
 * @see MouseListener
 * @see MouseMotionListener
 */
public class Panel_Terrain extends JPanel implements Runnable, 
													 MouseListener,
													 MouseMotionListener,
													 KeyListener
{
	private static final long serialVersionUID = 1L;
	
	//-------------------------
	//-- proprietes du panel --
	//-------------------------
	private static final Color COULEUR_FOND = new Color(50,200,50);
	private static final int LARGEUR = 500;
	private static final int HAUTEUR = 500;
	
	private static final float [] DASHES = {2.0F, 2.0F}; // trait tillé
	private static final BasicStroke TRAIT_TILLE = new BasicStroke(
			1.0f,BasicStroke.CAP_ROUND, 
            BasicStroke.JOIN_MITER, 
            10.0F, DASHES, 0.F);
	
	//------------
	//-- thread --
	//------------
	private Thread thread;
	private int TEMPS_REPOS_THREAD = 40;
	
	// souris
	private int sourisX, sourisY, // position reelle
				sourisCaseX, sourisCaseY; // position sur le cadriallage virtuel
	private boolean sourisSurTerrain;
	
	private int cadrillage = 10; // unite du cadriallage en pixel
	
	// gestion des interactions avec les tours 
	/**
	 * Le terrain permet de choisir la tour a poser sur le terrain
	 * Si cette variable est non nulle et que le joueur clique sur le
	 * terrain, la tour a ajouter sera posée.
	 */
	private Tour tourAAjouter;
	
	/**
	 * Lorsque le joueur clique sur une tour, elle devient selectionnee.
	 * Une fois selectionnee des informations sur la tour apparaissent
	 * dans le menu d'interaction. Le joueur pourra alors améliorer ou 
	 * detruire la tour.
	 */
	private Tour tourSelectionnee;
	
	// le jeu a gerer
	private Jeu jeu;
	
	private boolean afficherMaillage = true; // affichage du graphe ?

	private Fenetre_Jeu fenJeu;
	
	/**
	 * Constructeur du panel du terrain
	 * 
	 * @param jeu Le jeu a gerer
	 */
	public Panel_Terrain(Jeu jeu,Fenetre_Jeu fenJeu)
	{
		// sauvegarde du jeu
		this.jeu 	= jeu;
		this.fenJeu = fenJeu;
		
		// propriete du panel
		setPreferredSize(new Dimension(LARGEUR,HAUTEUR));
		
		// ajout des ecouteurs
		addKeyListener(this);
		setFocusable(true);

		addMouseListener(this);
		addMouseMotionListener(this);
		
		
		// demarrage du thread de rafraichissement
		thread = new Thread(this);
		thread.start();
	}
	
	/**
	 * Permet de modifier la tour a ajouter sur le terrain
	 * 
	 * @param tour la tour sélectionnée
	 */
	public void setTourAAjouter(Tour tour)
	{
		tour.setLocation(sourisCaseX, sourisCaseY);
		
		tourAAjouter = tour;

		if(tourAAjouter != null)
			tourSelectionnee = null;
	}

	/**
	 * Surdéfinition de la méthode d'affichage du panel.
	 * 
	 * Cette methode affiche la scene du jeu. Elle recupere differents 
	 * elements du jeu et les affiches.
	 * 
	 * @param g Le graphics du panel pour dessin
	 */
	public void paint(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		
		//--------------------------
		//-- affichage du terrain --
		//--------------------------
		g2.setColor(COULEUR_FOND);
		g2.fillRect(0, 0, LARGEUR, HAUTEUR);

		// comment faire une rotation ?
        //AffineTransform tx = new AffineTransform();
        //double radians = -Math.PI/4;
        //tx.rotate(radians);
		//g2.drawImage(terrain.getImageDeFond(), tx, this);
		
		if(jeu.getImageDeFondTerrain() != null)
			g2.drawImage(jeu.getImageDeFondTerrain(), 0, 0, null);
		
		
		//-------------------------------------------------
		//-- Affichage de la zone de depart et d'arrivee --
		//-------------------------------------------------
		
		g2.setColor(Color.GRAY);
		Rectangle zoneDepart = jeu.getZoneDepart();
		g2.fillRect((int) zoneDepart.getX(), (int) zoneDepart.getY(), 
					(int)zoneDepart.getWidth(), 
					(int)zoneDepart.getHeight());
		
		g2.setColor(Color.GRAY);
		Rectangle zoneArrivee = jeu.getZoneArrivee();
		g2.fillRect((int) zoneArrivee.getX(), (int) zoneArrivee.getY(), 
					(int)zoneArrivee.getWidth(), 
					(int)zoneArrivee.getHeight());
		
		//-------------------------------------
		//-- Affichage du grillage du graphe --
		//-------------------------------------
		if(afficherMaillage)
		{	
			
			ArrayList<Line2D> arcsActifs = jeu.getArcsActifs();
			
			if(arcsActifs != null)
				for(Line2D arc : arcsActifs)
				{
					g2.setColor(Color.GREEN);
					g2.drawLine((int)arc.getX1(),(int)arc.getY1(),
							(int)arc.getX2(),(int)arc.getY2());
				}
			/*
			ArrayList<Noeud> noeuds = jeu.getNoeuds();
			
			for(Noeud n : noeuds)
			{
				
				if(n.isActif())
					g2.setColor(Color.GREEN);
				else
					g2.setColor(Color.RED);
				
				g2.drawRect((int)n.getX(),(int)n.getY(),1,1);
			}
			*/
		}
		
		//-----------------------------
		//-- affichage des creatures --
		//-----------------------------
		
		Iterator<Creature> iCreatures = jeu.getCreatures().iterator();
		Creature creature;
		while(iCreatures.hasNext())
		{
			creature = iCreatures.next();
		
			g2.setColor(Color.YELLOW);
			
			if(creature.getImage() != null)
				g2.drawImage(creature.getImage(),
						(int) creature.getX(), (int) creature.getY(), 
						(int) creature.getWidth(), (int) creature.getHeight(), null);
			else
				g2.fillOval((int) creature.getCenterX(), 
						(int) creature.getCenterY(), 
						(int) creature.getWidth(), 
						(int) creature.getHeight());
			
			
			int largeurBarre = (int) (creature.getWidth() * 2);
			int hauteurBarre = 5;
			
			// affichage des barres de vie
			g2.setColor(Color.BLACK);
			g2.fillRect((int)creature.getCenterX(), 
					(int)(creature.getY()+creature.getHeight()+4), 
					largeurBarre, hauteurBarre);
			
			g2.setColor(Color.GREEN);
			g2.fillRect((int)creature.getCenterX(), 
					(int)(creature.getY()+creature.getHeight()+5), 
					(int)(creature.getSante()*largeurBarre/creature.getSanteMax()),
					hauteurBarre-2);
			
			// affichage du chemin des creatures
			if(afficherMaillage)
			{
				ArrayList<Point> chemin = creature.getChemin();
				if(chemin != null && chemin.size() > 0)
				{
					Point PointPrecedent = chemin.get(0);
					
					synchronized(chemin)
					{
						Iterator<Point> it = chemin.iterator();
						Point point;
						while(it.hasNext())
						{
							point = it.next();
								
							//g2.setColor(Color.GREEN);
							//g2.fillOval(point.x,point.y,4,4);
							
							g2.setColor(Color.BLUE);
							g2.drawLine(PointPrecedent.x, PointPrecedent.y, 
										point.x, point.y);
							PointPrecedent = point;
						}
					}
				}
			}
		}
		
		//-------------------------
		//-- affichage des tours --
		//-------------------------
		for(Tour tour : jeu.getTours())
			dessinerTour(tour,g2,false);
		
		
		//---------------------------------
		//-- entour la tour selectionnee --
		//---------------------------------
		if(tourSelectionnee != null)
		{
			dessinerTour(tourSelectionnee,g2,true);
			
			g2.setColor(Color.WHITE);
			g2.setStroke(TRAIT_TILLE);
			g2.drawRect(tourSelectionnee.getXi(), tourSelectionnee.getYi(),
					(int) (tourSelectionnee.getWidth()),
					(int) (tourSelectionnee.getHeight()));
		}
		
		//------------------------------------
		//-- affichage des rayons de portee --
		//------------------------------------
		/*
		if(afficherRayonsDePortee)
		{
			for(Tour tour : jeu.getTours())
			{
				g2.setColor(Color.WHITE);
				g2.drawOval((int)(tour.getXi() - tour.getRayonAction() / 2 + tour.getWidth()/2), 
							(int)(tour.getYi() - tour.getRayonAction() / 2 + tour.getHeight() / 2), 
						(int)tour.getRayonAction(), (int)tour.getRayonAction());
			}
		}
		*/
		
		//------------------------------------
		//-- affichage de la tour a ajouter --
		//------------------------------------
		if(tourAAjouter != null && sourisSurTerrain)
		{
			// dessin de la tour
			dessinerTour(tourAAjouter,g2,false);
			
			// positionnnable ou non
			if(!jeu.laTourPeutEtrePosee(tourAAjouter))
			{
				g2.setColor(Color.BLACK);
				g2.drawLine(sourisCaseX, sourisCaseY,
						(int) (sourisCaseX + tourAAjouter.getWidth()),
						(int) (sourisCaseY + tourAAjouter.getHeight()));
				
				g2.drawLine(sourisCaseX, (int) (sourisCaseY + tourAAjouter.getHeight()),
						(int) (sourisCaseX + tourAAjouter.getWidth()),
						sourisCaseY);
				
				g2.drawRect(sourisCaseX, sourisCaseY,
						(int) (tourAAjouter.getWidth()),
						(int) (tourAAjouter.getHeight()));
			}
			else
				// affichage du rayon de portee
				dessinerPortee(tourAAjouter,g2);
		}
	}
	
	private void dessinerTour(final Tour tour,
							  final Graphics2D g2,
							  final boolean avecPortee)
	{
		if(tour.getImage() != null)
			g2.drawImage(tour.getImage(), tour.getXi(), tour.getYi(), 
					(int)tour.getWidth(), 
					(int)tour.getHeight(),null);
		else
		{
			g2.setColor(tour.getCouleurDeFond());
			g2.fillRect(tour.getXi(), tour.getYi(), 
				(int)tour.getWidth(), 
				(int)tour.getHeight());
		}
		
		if(avecPortee)
			dessinerPortee(tour,g2);
	}
	
	
	private void dessinerPortee(Tour tour,Graphics2D g2)
	{
		// Set alpha.  0.0f is 100% transparent and 1.0f is 100% opaque.
        float alpha = .3f;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
		g2.setColor(tour.getCouleurDeFond());
		g2.drawOval((int)(tour.getXi() - tour.getRayonPortee() + tour.getWidth()/2), 
					(int)(tour.getYi() - tour.getRayonPortee() + tour.getHeight()/2), 
					(int)tour.getRayonPortee()*2, 
					(int)tour.getRayonPortee()*2);

        g2.setColor(Color.WHITE);
        g2.fillOval((int)(tour.getXi() - tour.getRayonPortee() + tour.getWidth()/2), 
        			(int)(tour.getYi() - tour.getRayonPortee() + tour.getHeight()/2), 
        			(int)tour.getRayonPortee()*2, 
        			(int)tour.getRayonPortee()*2);
        
        // remet la valeur initial
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
	}
	
	public void toggleAfficherMaillage()
	{
		afficherMaillage = !afficherMaillage;
	}
	
	
	/**
	 * Méthode de refraichissement du panel
	 * 
	 * L'implémentation de Runnable nous force à définir cette méthode.
	 * Celle-ci sera appelée lors du démarrage du thread.
	 * 
	 * @see Runnable
	 */
	public void run()
	{
		// Tant que la partie est en cours...
		while(true)
		{
			// Raffraichissement du panel
			repaint(); // -> appel de paint
			
			// Endore le thread
			try {
				Thread.sleep(TEMPS_REPOS_THREAD);
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Métode de gestion des cliques de la souris
	 * 
	 * @param me l'evenement lie a cette action 
	 * @see MouseListener
	 */
	public void mousePressed(MouseEvent me)
	{
		if (me.getButton() == MouseEvent.BUTTON1)
		{
			// la selection se fait lors du clique
			for(Tour tour : jeu.getTours()) // pour chaque tour... 
				if (tour.intersects(sourisX,sourisY,1,1)) // la souris est dedans ?
				{	
					if (tourSelectionnee == tour)
						tourSelectionnee = null; // deselection
					else
					{
						tourSelectionnee = tour; // la tour est selectionnee
						// si une tour est selectionnee, il n'y pas d'ajout
						tourAAjouter = null;  
					}
					
					fenJeu.tourSelectionnee(tourSelectionnee,
											Panel_InfoTour.MODE_SELECTION);
					return;
				}
		
			// aucun tour trouvee => clique dans le vide.
			tourSelectionnee = null;
			
			fenJeu.tourSelectionnee(tourSelectionnee,
					Panel_InfoTour.MODE_SELECTION);
		}
		else
		{
			// deselection total
			tourSelectionnee 	= null;
			tourAAjouter 		= null;
		}
	}
	
	/**
	 * Métode de gestion des relachements du clique de la souris
	 * 
	 * @param me l'evenement lie a cette action 
	 */
	public void mouseReleased(MouseEvent me)
	{
		// l'ajout se fait lors de la relache du clique
		if(tourAAjouter != null)
			fenJeu.acheterTour(tourAAjouter);
	}
	
	/**
	 * Méthode de gestion des deplacements de la souris
	 * 
	 * @param me evenement lie a cette action
	 * @see MouseMotionListener
	 */
	public void mouseMoved(MouseEvent me)
	{
		// mise a jour des coordonees de la souris
		sourisX = me.getX();
		sourisY = me.getY();
		
		// mise a jour de la position de la souris sur le grillage vituel
		sourisCaseX = Math.round(me.getX()/cadrillage)*cadrillage;
		sourisCaseY = Math.round(me.getY()/cadrillage)*cadrillage;
		
		if(tourAAjouter != null)
			tourAAjouter.setLocation(sourisCaseX, sourisCaseY);
	}

	/**
	 * Methode de gestion de la souris lorsque qu'elle entre dans le panel
	 * 
	 * @param me evenement lie a cette action
	 * @see MouseMotionListener
	 */
	public void mouseEntered(MouseEvent me)
	{
		sourisSurTerrain = true;
		
		// recuperation du focus. 
		// Important pour la gestion des touches clavier
		requestFocusInWindow(true); 
	}
	
	/**
	 * Methode de gestion de la souris lorsque qu'elle sort du panel
	 * 
	 * @param me evenement lie a cette action
	 * @see MouseMotionListener
	 */
	public void mouseExited(MouseEvent me)
	{
		sourisSurTerrain = false;
	}
	
	// methodes non redéfinies (voir MouseListener)
	public void mouseClicked(MouseEvent me){}
	public void mouseDragged(MouseEvent me){}

	public void setTourSelectionnee(Tour tour)
	{
		tourSelectionnee = tour;
	}

	/**
	 * Methode de gestion des evenements lors de la relache d'une touche
	 */
	public void keyReleased(KeyEvent ke)
	{
		if(tourSelectionnee != null)
		{
			// raccourci de vente
			if(ke.getKeyChar() == 'v' || ke.getKeyChar() == 'V')
				fenJeu.vendreTour(tourSelectionnee);
			// raccourci d'amelioration
			else if(ke.getKeyChar() == 'a' || ke.getKeyChar() == 'A')
				fenJeu.ameliorerTour(tourSelectionnee);
		}
	}
	public void keyPressed(KeyEvent ke){}
	public void keyTyped(KeyEvent ke){}

	public void deselectionner()
	{
		tourAAjouter 		= null;
		tourSelectionnee 	= null;
	}
}
