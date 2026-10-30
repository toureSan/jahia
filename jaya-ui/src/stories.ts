import { covers } from "./covers";

export interface Story {
  id: string;
  title: string;
  summary: string;
  paragraphs: string[];
  rubric: string;
  desk: "info" | "sport" | "culture" | "tv" | "audio";
  topic: "Suisse" | "Monde" | "Economie" | "Société" | "Sciences-Tech." | "Environnement" | "Sport" | "Culture";
  image: string;
  video?: string;
  duration?: string;
  publishedAt: string;
  author: string;
  place: string;
}

const clip = "/videos/reportage.mp4";

export const STORIES: Story[] = [
  {
    id: "tram-cedres",
    title: "Lausanne adopte le crédit de 186 millions pour la ligne jusqu'aux Cèdres",
    summary: "Le Conseil communal a dit oui par 68 voix contre 21. Les travaux commencent en mars 2027, la mise en service est visée pour décembre 2030.",
    rubric: "Suisse",
    desk: "info",
    topic: "Suisse",
    image: covers.city,
    video: clip,
    duration: "0:05",
    publishedAt: "2026-10-27T07:40:00.000Z",
    author: "Claire Morand",
    place: "Lausanne",
    paragraphs: [
      "Le Conseil communal de Lausanne a accepté lundi soir, peu après 22h, le crédit d'ouvrage de 186 millions de francs qui finance le prolongement de la ligne de bus à haut niveau de service jusqu'au quartier des Cèdres. Le texte passe par 68 oui, 21 non et 4 abstentions. La municipalité voulait un vote avant la fin du mois pour tenir le calendrier des appels d'offres.",
      "Le tracé mesure 3,4 kilomètres. Il part de la Bourdonnette, longe l'avenue de Provence, puis dessert deux nouvelles stations : Cèdres-Place et Cèdres-École. Les tl prévoient un bus toutes les six minutes aux heures de pointe, et toutes les dix minutes le reste de la journée. Le temps de parcours annoncé entre le centre et le terminus est de 14 minutes, contre 23 aujourd'hui aux heures chargées.",
      "« On ne construit pas une ligne pour le plan, on la construit pour les 4 800 habitants qui n'ont pas de desserte directe après 20h », a dit la municipale des travaux, Elsa Perrin, à la tribune. Elle a détaillé un phasage en trois étapes : déviation des réseaux en 2027, plateforme et stations en 2028-2029, essais et marche à blanc à l'automne 2030.",
      "L'opposition, emmenée par le groupe de Lucien Aubry, juge la facture sous-estimée. Elle cite le renchérissement des aciers et demande un plafond de 200 millions au-delà duquel le dossier reviendrait devant le conseil. Cet amendement a été refusé par 54 voix contre 31. Aubry a annoncé un recours au Tribunal cantonal sur la forme de l'enquête publique, pas sur le principe de la ligne.",
      "Le chantier de nuit sera limité à dix semaines, entre le 12 janvier et le 20 mars 2028, sur le seul tronçon de l'avenue de Provence. Les commerces du rez-de-chaussée recevront une indemnité forfaitaire de 80 francs par jour de fermeture d'accès, sur présentation des comptes. La municipalité ouvre une permanence le mardi à la Maison de quartier des Cèdres, de 16h à 19h, dès le 3 novembre."
    ]
  },
  {
    id: "leman-octobre",
    title: "Le Léman à 14,2 degrés : les plages de Vidy restent ouvertes une semaine de plus",
    summary: "La mesure prise à un mètre de profondeur, au large de l'embouchure du Flon, dépasse de 1,8 degré la moyenne des vingt dernières années pour un 26 octobre.",
    rubric: "Environnement",
    desk: "info",
    topic: "Environnement",
    image: covers.banner,
    video: clip,
    duration: "0:05",
    publishedAt: "2026-10-27T06:15:00.000Z",
    author: "Nadia El Amrani",
    place: "Genève",
    paragraphs: [
      "Le service cantonal de l'eau a relevé lundi 26 octobre, à 8h, une température de 14,2 °C dans le Léman, à un mètre sous la surface et à 400 mètres au large de l'embouchure du Flon. La moyenne 2006-2025 pour cette date est de 12,4 °C. Le relevé de fond, à 40 mètres, reste stable à 6,1 °C.",
      "Conséquence immédiate : la Ville de Genève maintient les postes de secours de Vidy et des Eaux-Vives jusqu'au dimanche 2 novembre, au lieu de les fermer le 26 octobre comme le prévoyait le calendrier. Deux maîtres-nageurs seront présents de 11h à 17h. La baignade hors de ces horaires n'est pas interdite, mais elle se fait sans surveillance.",
      "« Ce n'est pas un record isolé, c'est le sixième mois consécutif au-dessus de la norme », explique Martin Joliat, limnologue à l'Université de Genève, qui suit la bouée de mesure au large de Nyon. Il rappelle que le lac a déjà perdu, en surface, une semaine de brassage hivernal complet lors des trois derniers hivers doux. Sans ce brassage, l'oxygène peine à redescendre vers les couches profondes.",
      "Les pêcheurs professionnels du canton ont débarqué 18 tonnes de perche en septembre, contre 27 tonnes le même mois en 2023. La commission de la pêche se réunit le 4 novembre pour décider si le quota de filets dormants sera réduit de 10 % dès décembre. La décision sera publiée le soir même sur le site du canton.",
      "La Ville demande aux usagers de ne pas nourrir les oiseaux sur les quais : avec l'eau plus tiède, les cyanobactéries restent visibles dans deux criques de la rive droite. Les panneaux jaunes posés samedi à la Perle du Lac et à Genève-Plage signalent un risque pour les chiens qui boivent l'eau du bord. L'eau du réseau, elle, n'est pas concernée."
    ]
  },
  {
    id: "horlogerie-septembre",
    title: "Les exportations horlogères reculent de 4,3 % en septembre, tirées par la Chine",
    summary: "Le mois se solde à 2,14 milliards de francs. Les États-Unis progressent de 6 %, l'Europe reste plate, et les garde-temps de moins de 200 francs portent la baisse.",
    rubric: "Economie",
    desk: "info",
    topic: "Economie",
    image: covers.newsroom,
    publishedAt: "2026-10-26T16:05:00.000Z",
    author: "Marc Bovey",
    place: "Bienne",
    paragraphs: [
      "La Fédération horlogère a publié dimanche des chiffres que les ateliers attendaient depuis dix jours : 2,14 milliards de francs d'exportations en septembre, soit 4,3 % de moins qu'en septembre 2025. Sur neuf mois, le cumul reste positif de 0,8 %, à 19,6 milliards. Le trou vient presque entier du segment inférieur à 200 francs, en recul de 11 %.",
      "La Chine et Hong Kong représentent à eux deux 310 millions de francs de moins sur le seul mois. Les stocks chez les détaillants de Shanghai et de Shenzhen ne se sont pas écoulés pendant les soldes d'été. « On ne coupe pas la production, on décale les livraisons de novembre à janvier », dit Hélène Chapuis, directrice d'une manufacture de 420 personnes au Locle, qui tourne actuellement à 86 % de sa capacité.",
      "À l'inverse, les États-Unis achètent pour 390 millions, en hausse de 6 %. Le créneau de 3 000 à 8 000 francs fait le gros du volume. L'Europe est stable à 0,2 % près. L'Allemagne baisse de 3 %, la France monte de 2 %, le Royaume-Uni ne bouge pas.",
      "L'emploi, lui, ne suit pas encore la courbe. Le secteur compte 64 800 postes en Suisse à fin septembre, 400 de plus qu'il y a un an, surtout dans le sertissage et le polissage en vallée de Joux. Les annonces de chômage partiel restent rares : deux maisons, à La Chaux-de-Fonds, ont déposé un préavis pour 90 personnes, limité à six semaines après les fêtes.",
      "La prochaine photographie sera celle d'octobre, publiée le 19 novembre. Les détaillants interrogés à Genève et à Zurich disent que les commandes de fin d'année sont signées, mais plus tardives : beaucoup attendent la première semaine de novembre pour ajuster les quantités. Chapuis, elle, a déjà gelé les heures supplémentaires jusqu'au 15 janvier."
    ]
  },
  {
    id: "lits-geriatrie",
    title: "Genève ouvre 42 lits de gériatrie à Beau-Séjour, la liste d'attente tombe à onze jours",
    summary: "L'unité accueille ses premiers patients mercredi. Le canton finance 6,8 millions par an, et 38 postes sont déjà pourvus sur les 51 prévus.",
    rubric: "Société",
    desk: "info",
    topic: "Société",
    image: covers.archive,
    publishedAt: "2026-10-26T11:20:00.000Z",
    author: "Aline Rochat",
    place: "Genève",
    paragraphs: [
      "Les Hôpitaux universitaires de Genève ouvrent mercredi 28 octobre une unité de 42 lits aux étages 4 et 5 du bâtiment de Beau-Séjour. Elle est réservée aux patients de plus de 75 ans qui n'ont plus besoin de soins aigus mais qui ne peuvent pas encore rentrer chez eux. Jusqu'ici, ces personnes restaient en moyenne 9 jours de trop dans un lit de médecine.",
      "La liste d'attente cantonale pour une place de réadaptation comptait 27 jours au 1er octobre. La direction vise 11 jours d'ici au 15 décembre, le temps que les équipes prennent leur rythme. « On ne règle pas le manque de places en EMS, on évite que l'hôpital serve de salle d'attente », résume le docteur Karim Bensaïd, chef du service.",
      "Le coût de fonctionnement est de 6,8 millions de francs par an, pris en charge par le canton à 55 % et par les assureurs à 45 % selon la clé habituelle. Le crédit d'investissement, 4,1 millions pour les lits, les rails de transfert et la salle de kinésithérapie, a été voté en juin.",
      "Côté personnel, 38 des 51 postes sont pourvus : 22 infirmières et infirmiers, 9 aides-soignants, 4 physiothérapeutes, 2 ergothérapeutes et 1 assistante sociale. Les 13 postes restants sont des remplacements de nuit. Les HUG disent recevoir encore des dossiers, avec une prise de poste possible au 1er décembre. Le salaire d'entrée annoncé pour une infirmière diplômée est de 6 450 francs brut pour un plein temps.",
      "Les familles peuvent visiter de 13h à 20h, sans rendez-vous. Une ligne directe, le 022 372 44 10, répond du lundi au vendredi de 8h à 17h pour savoir si un proche a été transféré. Le premier bilan chiffré — durée de séjour, retours à domicile, orientations en EMS — sera présenté à la commission de la santé le 12 janvier."
    ]
  },
  {
    id: "corridor-rail",
    title: "Genève : accord de principe sur le corridor ferroviaire Lyon–Milan, sans budget voté",
    summary: "Les délégations ont signé une feuille de route de 14 pages. Le prochain train direct supplémentaire n'est pas promis avant l'horaire 2028.",
    rubric: "Monde",
    desk: "info",
    topic: "Monde",
    image: covers.city,
    publishedAt: "2026-10-25T18:45:00.000Z",
    author: "Julien Pasche",
    place: "Genève",
    paragraphs: [
      "Dans une salle du Palais Eynard, les délégations française, italienne et suisse ont signé samedi une feuille de route de 14 pages sur le corridor ferroviaire Lyon–Genève–Milan. Le texte ne débloque pas d'argent. Il fixe trois rendez-vous : un horaire cible, une liste de goulets, et un rapport de capacité pour mars 2027.",
      "Aujourd'hui, le meilleur trajet Lyon–Milan via Genève demande 6h05, avec une correspondance à Genève ou à Lausanne selon les sillons. L'objectif écrit dans l'accord est de 5h15, sans changement, deux fois par jour, à l'horaire de décembre 2028. Les CFF précisent que cela suppose deux rames bicourant de plus et un créneau de 12 minutes libéré entre Genève-Cornavin et Annemasse le matin.",
      "Le point dur reste le nœud de Culoz, côté français, où les trains de fret du soir occupent les voies jusqu'à 21h40. La délégation française s'est engagée à proposer, d'ici février, deux scénarios : un report du fret après 22h30, ou un troisième quai. Aucun des deux n'a de financement inscrit au budget 2027.",
      "Côté italien, le tronçon Domodossola–Milan est décrit comme « saturé entre 7h et 9h ». Le rapport de mars devra dire combien de sillons voyageurs peuvent être ajoutés sans retirer de fret. La conseillère fédérale chargée des transports, présente une heure en fin de séance, a refusé de chiffrer une participation suisse. « On paie ce qui roule sur notre réseau, pas les quais des autres », a-t-elle dit aux journalistes sur le perron.",
      "Les associations d'usagers, elles, demandaient un train de plus dès décembre 2026, le vendredi soir. Elles ne l'auront pas. La feuille de route prévoit seulement une enquête auprès de 2 000 voyageurs, lancée en janvier, pour savoir s'ils préfèrent un départ de Lyon à 6h12 ou à 7h02. Les résultats seront publics."
    ]
  },
  {
    id: "lavaux-gel",
    title: "Lavaux : le gel du 19 avril a pris 30 % du chasselas à Calamin",
    summary: "Le vigneron André Pichon chiffre la perte à 11 000 litres sur ses 4,2 hectares. Le canton ouvre un guichet d'aide de 1 500 francs par hectare touché.",
    rubric: "Environnement",
    desk: "info",
    topic: "Environnement",
    image: covers.archive,
    video: clip,
    duration: "0:05",
    publishedAt: "2026-10-25T09:00:00.000Z",
    author: "Léa Fontaine",
    place: "Epesses",
    paragraphs: [
      "Dans les terrasses de Calamin, au-dessus d'Epesses, André Pichon montre des sarments courts et des grappes clairsemées. La nuit du 19 avril, le thermomètre de sa parcelle haute est descendu à -2,4 °C pendant trois heures. Le chasselas, déjà débourré, a perdu les bourgeons bas. Il estime la récolte 2026 à 11 000 litres, contre 16 000 une année pleine, sur 4,2 hectares.",
      "Le bilan cantonal, consolidé vendredi par les offices de la viticulture, parle de 30 % de volume en moins sur l'appellation Calamin, et de 12 % sur l'ensemble de Lavaux. Dézaley s'en sort mieux : les parcelles face au lac sont restées à -0,5 °C. « Le lac a fait son travail, les coteaux du haut non », résume Pichon.",
      "Le Conseil d'État ouvre lundi un guichet jusqu'au 30 novembre. L'aide est de 1 500 francs par hectare dont le rendement tombe sous 60 % de la moyenne quinquennale, sur facture d'un contrôleur agréé. Le plafond par exploitation est de 18 000 francs. Pichon, à 4,2 hectares touchés, peut prétendre à 6 300 francs. « Ça paie les bougies de l'an prochain, pas le vin qui manque », dit-il.",
      "Il a déjà commandé 420 bougies de paraffine à 4,80 francs pièce, à allumer dès que MétéoSuisse annonce moins de 1 °C après le débourrement. La nuit du 19 avril, il n'en avait que 80. Le fournisseur de Vevey livre sous huit jours, à condition de commander avant le 15 mars. Plusieurs voisins groupent une commande pour baisser le transport.",
      "La mise en bouteille du millésime court se fera en juin 2027, avec une étiquette inchangée mais un volume réduit. Pichon ne montera pas le prix : 18 francs la bouteille au caveau, 11 francs au domaine pour les allocataires du village. Il tiendra une porte ouverte le samedi 7 novembre, de 10h à 16h, pour expliquer la récolte aux clients habituels plutôt que d'envoyer une lettre."
    ]
  },
  {
    id: "hc-leman",
    title: "Le HC Léman arrache la victoire 3-2 contre les Alpes à la 64e minute",
    summary: "Le but de Nina Keller, sur un rush à deux, donne les trois points au club lausannois. La gardienne a sorti 31 tirs.",
    rubric: "Sport",
    desk: "sport",
    topic: "Sport",
    image: covers.security,
    video: clip,
    duration: "0:05",
    publishedAt: "2026-10-26T21:55:00.000Z",
    author: "Romain Keller",
    place: "Malley",
    paragraphs: [
      "Il a fallu attendre la 64e minute et un rush mal défendu pour que le HC Léman trouve la faille. Nina Keller, entrée à la 50e à la place de Sara Imhof, a pris le palet sur la ligne bleue, a déposé Inès Rochat sur sa droite et a ajusté le coin supérieur. 3-2, et la patinoire de Malley debout. Les Alpes n'ont plus trouvé le chemin du but.",
      "Le match avait mal commencé pour les locales. À la 7e, un palet dévié par une jambière donnait l'avantage aux visiteuses. Léman égalisait à la 18e par Elsa Graf, sur un jeu de puissance concédé pour une crosse haute. Les Alpes repassaient devant à la 33e, sur un deux-contre-un mal couvert par la défense de droite. Le vestiaire, à la pause, était à 1-2.",
      "La gardienne du Léman, Maëlle Conti, a fini avec 31 arrêts, dont un arrêt réflexe à la 58e sur un tir des 8 mètres. « Sans elle on rentre au vestiaire avec zéro point », a dit l'entraîneur Patrick Morier. Il a aussi assumé le changement de la 50e : « Keller patine plus vite, il me fallait une joueuse fraîche pour les dix dernières minutes. »",
      "Au classement, Léman passe à 13 points en sept matches, à deux longueurs des Alpes, qui restent premières. Le prochain rendez-vous est samedi 31 octobre, 18h30, à Fribourg, contre le HC Sarine. Morier n'a pas encore dit si Keller partirait titulaire. Imhof, auteur d'une aide sur le but de Graf, « a sa place », selon lui.",
      "La billetterie de samedi ouvre en ligne mercredi à 10h. Les places debout du virage nord sont à 22 francs, les sièges latéraux à 38 francs. Le club rappelle que le portique ouvre 45 minutes avant la mise en jeu et que les sacs de plus de 30 litres ne passent pas."
    ]
  },
  {
    id: "festival-du-lac",
    title: "Nyon : le Festival du lac annonce 42 concerts et des billets à 45 francs la journée",
    summary: "La 3e édition se tient du 3 au 5 juillet sur la pelouse de Colovray. Trois scènes, un dernier métro à 0h40, et 18 000 places par soir.",
    rubric: "Culture",
    desk: "culture",
    topic: "Culture",
    image: covers.newsroom,
    publishedAt: "2026-10-24T14:10:00.000Z",
    author: "Sophie Mercier",
    place: "Nyon",
    paragraphs: [
      "Le Festival du lac a dévoilé vendredi son affiche 2027 : 42 concerts en trois soirs, du 3 au 5 juillet, sur la pelouse de Colovray à Nyon. La capacité est plafonnée à 18 000 personnes par soir, soit 2 000 de moins que l'an dernier, pour élargir les allées après les bousculades du samedi 2025 au moment du rappel de 23h15.",
      "La journée coûte 45 francs en prévente jusqu'au 30 novembre, puis 55 francs. Le pass trois jours est à 110 francs, parking non compris. Les moins de 16 ans accompagnés entrent à 15 francs. Les billets se prennent uniquement en ligne, par nominatif, avec une pièce d'identité au contrôle. La directrice, Camille Arnold, assume : « On a eu 400 billets revendus trois fois plus cher l'an dernier. On ferme cette porte. »",
      "Trois scènes. La grande, face au lac, accueille les têtes d'affiche à 21h30. La tente des Cèdres, 3 000 places assises, programme le jazz et les formations locales dès 17h. Le quai, en accès libre avant 19h, reçoit les découvertes. Parmi les noms confirmés : le quatuor Les Rives, la chanteuse Maya Soler, et l'orchestre de cuivres d'Yverdon pour la fermeture de dimanche.",
      "Les CFF ajoutent un train direct Genève–Nyon à 0h40 les trois soirs, à la demande de la Ville. Le retour vers Lausanne part à 0h25 et 0h55. Arnold demande aux festivaliers de ne pas compter sur le parking du stade : 1 200 places seulement, à 15 francs, réservées d'abord aux personnes à mobilité réduite et aux familles avec poussette.",
      "Le montage commence le 21 juin. Les riverains de la route de Saint-Cergue recevront, comme l'an dernier, un bon de 30 francs par soir pour une chambre d'hôtel ou des bouchons d'oreille, sur inscription avant le 1er juin à la voirie. La soirée de dimanche se termine à 22h30 précises, condition posée par le canton pour renouveler l'autorisation."
    ]
  },
  {
    id: "quai-renens",
    title: "Renens : un quai de plus et un train toutes les 15 minutes dès le 13 décembre",
    summary: "Les travaux de nuit durent du 3 au 21 novembre. Trois bus remplacent les trains entre Renens et Lausanne de 22h40 à 5h.",
    rubric: "Sciences-Tech.",
    desk: "info",
    topic: "Sciences-Tech.",
    image: covers.servers,
    publishedAt: "2026-10-24T08:30:00.000Z",
    author: "Marc Bovey",
    place: "Renens",
    paragraphs: [
      "Dès le dimanche 13 décembre, le quai 4 de Renens entre en service et la cadence entre Renens et Lausanne passe à un train toutes les 15 minutes, de 6h à 20h, puis toutes les 30 minutes jusqu'à 0h30. C'est le dernier morceau du plan de gare voté en 2022. Le quai mesure 220 mètres et accepte les compositions à deux étages.",
      "Pour y arriver, les CFF ferment les voies 2 et 3 la nuit, du mardi 3 novembre au vendredi 21 novembre, entre 22h40 et 5h. Trois bus partent de la place de la gare de Renens à 22h50, 23h20, 23h50, 0h30 et 1h10, et s'arrêtent à Prilly-Malley puis à Lausanne. Le trajet annoncé est de 18 minutes, contre 7 en train. Les vélos montent dans le troisième bus uniquement.",
      "Le jour, le trafic voyageurs ne change pas. Les voyageurs qui prennent le premier train de 5h12 verront encore l'ancien quai jusqu'au 12 décembre. L'affichage en gare bascule dans la nuit du 12 au 13. Les CFF conseillent de recharger l'application ce soir-là : les anciens numéros de quai resteront en mémoire sinon.",
      "Le coût final du quai est de 47 millions de francs, 3 millions au-dessus du crédit, à cause du déplacement d'une conduite de gaz découverte en juin. Le surcoût est pris sur la réserve de la convention canton-CFF, sans nouveau vote. La municipalité de Renens a toutefois exigé un passage sous-voies supplémentaire, livré, avec un ascenseur de chaque côté.",
      "Une permanence CFF s'installe dans le hall de la gare les samedis 7 et 14 novembre, de 9h à 13h, pour les abonnés qui veulent vérifier leur correspondance. Le chef de gare, Paul Henriod, le dit simplement : « Le 13 décembre au matin, suivez les flèches jaunes, pas votre habitude. »"
    ]
  },
  {
    id: "forum-matin",
    title: "Le débat du matin : faut-il avancer la fermeture des terrasses à 22h en semaine ?",
    summary: "Une municipale, un cafetier et une habitante du centre ont débattu 18 minutes. Le vote consultatif de la Ville est prévu le 29 novembre.",
    rubric: "Audio",
    desk: "audio",
    topic: "Société",
    image: covers.newsroom,
    publishedAt: "2026-10-27T05:50:00.000Z",
    author: "Émission Le matin du clone",
    place: "Lausanne",
    paragraphs: [
      "Invités de 7h15 : Elsa Perrin, municipale ; Nico Beltrametti, qui tient le café du Grand-Pont depuis 2009 ; et Jeanne Chollet, habitante de la rue Centrale depuis 22 ans. Le sujet tient en une ligne du règlement que la municipalité veut modifier : en semaine, les terrasses devraient rentrer le mobilier à 22h au lieu de 23h30. Le week-end ne change pas.",
      "Perrin défend le texte avec des chiffres de la police municipale : 146 interventions pour tapage lié aux terrasses entre mai et septembre, dont 90 après 22h30. « On ne ferme pas les cafés, on demande que les chaises rentrent. L'intérieur peut continuer. » Elle rappelle que le vote du 29 novembre est consultatif. Le règlement, lui, se vote au conseil en janvier.",
      "Beltrametti sort sa caisse. Sur un mardi d'octobre, le service de 22h à 23h30 représente 380 francs, soit 17 % du chiffre du soir. « Si vous coupez ça, je sors un employé à 21h30. Ce n'est pas un débat abstrait, c'est une personne. » Il propose un compromis : 23h du lundi au jeudi, 23h30 le vendredi, à condition que le café désigne un responsable du bruit identifiable par un badge.",
      "Chollet, elle, décrit le bruit tel qu'elle l'entend au troisième étage, fenêtres fermées : « À 23h10 les chaises raclent, à 23h20 les groupes restent sur le trottoir. Le règlement parle des terrasses, pas du trottoir. » Elle demande que l'heure s'applique aussi au stationnement devant la porte, et que la police passe vraiment à 22h15, pas seulement sur appel.",
      "Aucun des trois n'a lâché sur l'heure pile. Ils se sont retrouvés sur un point : un état des lieux bruyant, café par café, publié avant le vote du 29 novembre. Perrin a dit oui, à condition que les mesures soient faites un mardi et un samedi, par le service de l'environnement, sans prévenir l'exploitant. Beltrametti a accepté le mardi, pas le samedi surprise. Chollet veut les deux. La suite du débat est résumée dans le procès-verbal, en ligne dès midi sur le site de la Ville."
    ]
  }
];
