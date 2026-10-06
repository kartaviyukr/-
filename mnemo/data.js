/*
 * Встроенные наборы слов. Формат строки карточки:
 *   термин | перевод | пример | перевод примера
 * Пример и его перевод необязательны. Варианты перевода — через запятую.
 * id карточки строится из id набора и термина, поэтому новые слова можно
 * добавлять в любое место — прогресс по старым словам не потеряется.
 */
window.MNEMO_DECKS = [
{
  id: 'en-verbs', emoji: '🏃', title: 'Самые нужные глаголы', level: 'A1', front: 'en-US', back: 'ru-RU',
  desc: '60 глаголов, на которых держится половина любой беседы.',
  cards: `
be|быть|I want to be a doctor.|Я хочу быть врачом.
have|иметь|I have two sisters.|У меня две сестры.
do|делать|What do you do on weekends?|Что ты делаешь по выходным?
say|сказать|She said nothing.|Она ничего не сказала.
go|идти, ехать|Let's go home.|Пойдём домой.
get|получать, доставать|I got a letter today.|Сегодня я получил письмо.
make|делать, создавать|She makes great coffee.|Она варит отличный кофе.
know|знать|I know the answer.|Я знаю ответ.
think|думать|I think you're right.|Думаю, ты прав.
take|брать|Take an umbrella.|Возьми зонт.
see|видеть|I can see the sea.|Я вижу море.
come|приходить|Come here, please.|Подойди сюда, пожалуйста.
want|хотеть|I want some tea.|Я хочу чаю.
look|смотреть|Look at this picture.|Посмотри на эту картинку.
use|использовать|Can I use your phone?|Можно воспользоваться твоим телефоном?
find|находить|I can't find my keys.|Не могу найти ключи.
give|давать|Give me a minute.|Дай мне минутку.
tell|рассказывать, сообщать|Tell me about your day.|Расскажи мне о своём дне.
work|работать|He works in a bank.|Он работает в банке.
call|звонить, называть|Call me tomorrow.|Позвони мне завтра.
try|пытаться, пробовать|Try this cake.|Попробуй этот торт.
ask|спрашивать, просить|Can I ask a question?|Можно задать вопрос?
need|нуждаться|I need help.|Мне нужна помощь.
feel|чувствовать|I feel tired.|Я чувствую усталость.
become|становиться|It became cold.|Стало холодно.
leave|уходить, оставлять|Don't leave me alone.|Не оставляй меня одного.
put|класть, ставить|Put the book on the table.|Положи книгу на стол.
mean|означать, иметь в виду|What does this word mean?|Что означает это слово?
keep|хранить, продолжать|Keep calm.|Сохраняй спокойствие.
let|позволять|Let me help you.|Позволь мне помочь.
begin|начинать|The lesson begins at nine.|Урок начинается в девять.
help|помогать|Can you help me?|Можешь мне помочь?
talk|разговаривать|We talked for hours.|Мы разговаривали часами.
turn|поворачивать|Turn left at the corner.|На углу поверните налево.
start|начинать|Let's start now.|Давай начнём сейчас.
show|показывать|Show me your photos.|Покажи мне свои фото.
hear|слышать|I can't hear you.|Я тебя не слышу.
play|играть|Kids play in the park.|Дети играют в парке.
run|бегать|I run every morning.|Я бегаю каждое утро.
move|двигаться, переезжать|We moved to a new flat.|Мы переехали в новую квартиру.
live|жить|I live in Moscow.|Я живу в Москве.
believe|верить|I believe you.|Я тебе верю.
bring|приносить|Bring your friend.|Приводи своего друга.
happen|случаться|What happened?|Что случилось?
write|писать|Write your name here.|Напишите здесь своё имя.
sit|сидеть|Sit down, please.|Садитесь, пожалуйста.
stand|стоять|Stand up.|Встаньте.
lose|терять, проигрывать|I lost my wallet.|Я потерял кошелёк.
pay|платить|Can I pay by card?|Можно оплатить картой?
meet|встречать|Nice to meet you.|Приятно познакомиться.
learn|учить, узнавать|I learn English every day.|Я учу английский каждый день.
change|менять|People change.|Люди меняются.
understand|понимать|I don't understand.|Я не понимаю.
read|читать|I read before bed.|Я читаю перед сном.
spend|тратить, проводить|We spent a week in Rome.|Мы провели неделю в Риме.
remember|помнить|I remember your name.|Я помню твоё имя.
forget|забывать|Don't forget your keys.|Не забудь ключи.
buy|покупать|I need to buy some milk.|Мне нужно купить молока.
wait|ждать|Wait for me!|Подожди меня!
open|открывать|Open the window, please.|Открой окно, пожалуйста.
close|закрывать|Close the door.|Закрой дверь.
sleep|спать|I sleep eight hours.|Я сплю восемь часов.
eat|есть|Let's eat something.|Давай что-нибудь поедим.
drink|пить|Drink more water.|Пей больше воды.
love|любить|I love you.|Я тебя люблю.
`},
{
  id: 'en-adj', emoji: '🎨', title: 'Прилагательные-пары', level: 'A1', front: 'en-US', back: 'ru-RU',
  desc: 'Противоположности запоминаются парами — учите их вместе.',
  cards: `
big|большой|It's a big house.|Это большой дом.
small|маленький|A small cup of coffee.|Маленькая чашка кофе.
long|длинный|She has long hair.|У неё длинные волосы.
short|короткий, низкий|It was a short trip.|Это была короткая поездка.
old|старый|My car is old.|Моя машина старая.
new|новый|I have a new phone.|У меня новый телефон.
young|молодой|He is very young.|Он очень молод.
good|хороший|Have a good day!|Хорошего дня!
bad|плохой|That's a bad idea.|Это плохая идея.
hot|горячий, жаркий|It's hot today.|Сегодня жарко.
cold|холодный|The water is cold.|Вода холодная.
warm|тёплый|A warm jacket.|Тёплая куртка.
cheap|дешёвый|This hotel is cheap.|Этот отель дешёвый.
expensive|дорогой|The ring is expensive.|Кольцо дорогое.
easy|лёгкий, простой|The test was easy.|Тест был лёгким.
difficult|трудный|English isn't difficult.|Английский не трудный.
fast|быстрый|A fast train.|Скоростной поезд.
slow|медленный|The internet is slow.|Интернет медленный.
early|ранний, рано|I get up early.|Я встаю рано.
late|поздний, поздно|Sorry, I'm late.|Извините, я опоздал.
full|полный, сытый|I'm full, thanks.|Я сыт, спасибо.
empty|пустой|The fridge is empty.|Холодильник пуст.
clean|чистый|Clean hands.|Чистые руки.
dirty|грязный|Your shoes are dirty.|У тебя грязные ботинки.
heavy|тяжёлый|This bag is heavy.|Эта сумка тяжёлая.
light|лёгкий, светлый|A light breakfast.|Лёгкий завтрак.
strong|сильный|Strong coffee.|Крепкий кофе.
weak|слабый|I feel weak.|Я чувствую слабость.
rich|богатый|A rich man.|Богатый человек.
poor|бедный|A poor country.|Бедная страна.
happy|счастливый|I'm happy to see you.|Я рад тебя видеть.
sad|грустный|Why are you sad?|Почему ты грустишь?
right|правильный, правый|That's right.|Верно.
wrong|неправильный|Wrong number.|Не туда попали.
quiet|тихий|A quiet street.|Тихая улица.
loud|громкий|The music is too loud.|Музыка слишком громкая.
beautiful|красивый|What a beautiful view!|Какой красивый вид!
ugly|уродливый|An ugly building.|Уродливое здание.
busy|занятой|I'm busy now.|Я сейчас занят.
free|свободный, бесплатный|Are you free tonight?|Ты свободен вечером?
safe|безопасный|Is it safe here?|Здесь безопасно?
dangerous|опасный|A dangerous road.|Опасная дорога.
same|одинаковый, тот же|We have the same bag.|У нас одинаковые сумки.
different|разный, другой|We are very different.|Мы очень разные.
important|важный|It's very important.|Это очень важно.
interesting|интересный|An interesting book.|Интересная книга.
boring|скучный|The film was boring.|Фильм был скучным.
tired|уставший|I'm so tired.|Я так устал.
hungry|голодный|Are you hungry?|Ты голоден?
`},
{
  id: 'en-food', emoji: '🍎', title: 'Еда и напитки', level: 'A1', front: 'en-US', back: 'ru-RU',
  desc: 'Продукты, блюда и всё, что пригодится в магазине и кафе.',
  cards: `
bread|хлеб|Fresh bread smells great.|Свежий хлеб отлично пахнет.
butter|сливочное масло|Bread and butter.|Хлеб с маслом.
cheese|сыр|I love French cheese.|Я люблю французский сыр.
egg|яйцо|Two boiled eggs, please.|Два варёных яйца, пожалуйста.
milk|молоко|A glass of milk.|Стакан молока.
meat|мясо|I don't eat meat.|Я не ем мясо.
chicken|курица|Chicken soup.|Куриный суп.
beef|говядина|Beef steak.|Говяжий стейк.
pork|свинина|Pork chops.|Свиные отбивные.
fish|рыба|Fish and chips.|Рыба с картошкой фри.
rice|рис|Rice with vegetables.|Рис с овощами.
potato|картофель|Mashed potatoes.|Картофельное пюре.
vegetables|овощи|Eat more vegetables.|Ешь больше овощей.
tomato|помидор|Tomato salad.|Салат из помидоров.
cucumber|огурец|A fresh cucumber.|Свежий огурец.
onion|лук|Onions make me cry.|От лука я плачу.
garlic|чеснок|Garlic bread.|Чесночный хлеб.
carrot|морковь|Carrot juice.|Морковный сок.
cabbage|капуста|Cabbage soup.|Щи.
mushroom|гриб|Mushroom pizza.|Пицца с грибами.
apple|яблоко|An apple a day keeps the doctor away.|Яблоко в день — и доктор не нужен.
pear|груша|A ripe pear.|Спелая груша.
banana|банан|A banana smoothie.|Банановый смузи.
grapes|виноград|A bunch of grapes.|Гроздь винограда.
strawberry|клубника|Strawberry jam.|Клубничное варенье.
lemon|лимон|Tea with lemon.|Чай с лимоном.
orange|апельсин|Orange juice.|Апельсиновый сок.
sugar|сахар|No sugar, please.|Без сахара, пожалуйста.
salt|соль|Pass the salt, please.|Передай соль, пожалуйста.
pepper|перец|Salt and pepper.|Соль и перец.
flour|мука|A kilo of flour.|Килограмм муки.
oil|растительное масло|Olive oil.|Оливковое масло.
honey|мёд|Tea with honey.|Чай с мёдом.
water|вода|Still or sparkling water?|Вода без газа или с газом?
juice|сок|Apple juice.|Яблочный сок.
tea|чай|Green tea.|Зелёный чай.
coffee|кофе|Black coffee.|Чёрный кофе.
wine|вино|Red wine.|Красное вино.
beer|пиво|A pint of beer.|Пинта пива.
breakfast|завтрак|What's for breakfast?|Что на завтрак?
lunch|обед|Let's have lunch together.|Давай пообедаем вместе.
dinner|ужин|Dinner is ready!|Ужин готов!
dessert|десерт|Any room for dessert?|Найдётся место для десерта?
soup|суп|Tomato soup.|Томатный суп.
sandwich|бутерброд, сэндвич|A ham sandwich.|Сэндвич с ветчиной.
delicious|очень вкусный|This cake is delicious.|Этот торт очень вкусный.
spicy|острый (о еде)|Too spicy for me.|Слишком остро для меня.
sweet|сладкий|Sweet dreams!|Сладких снов!
sour|кислый|Sour cream.|Сметана.
bitter|горький|Bitter chocolate.|Горький шоколад.
`},
{
  id: 'en-cafe', emoji: '☕', title: 'В кафе и ресторане', level: 'A2', front: 'en-US', back: 'ru-RU',
  desc: 'Готовые фразы: заказать, уточнить, попросить счёт.',
  cards: `
a table for two, please|столик на двоих, пожалуйста|A table for two, please, by the window.|Столик на двоих, пожалуйста, у окна.
Can I see the menu?|Можно меню?|Can I see the menu, please?|Можно, пожалуйста, меню?
I'd like…|Я бы хотел…|I'd like a cappuccino.|Я бы хотел капучино.
What do you recommend?|Что вы посоветуете?|What do you recommend for a starter?|Что посоветуете на закуску?
starter|закуска|Soup for a starter.|Суп в качестве закуски.
main course|основное блюдо|Steak for the main course.|Стейк на основное.
side dish|гарнир|Rice as a side dish.|Рис на гарнир.
Is it spicy?|Это острое?|Is the curry spicy?|Карри острое?
I'm allergic to nuts|у меня аллергия на орехи|I'm allergic to nuts, is there any in this?|У меня аллергия на орехи, они тут есть?
vegetarian|вегетарианский|Do you have vegetarian dishes?|У вас есть вегетарианские блюда?
to go|с собой|One coffee to go.|Один кофе с собой.
for here|здесь (не с собой)|For here or to go?|Здесь или с собой?
The bill, please|Счёт, пожалуйста|Excuse me, the bill, please.|Извините, счёт, пожалуйста.
tip|чаевые|Is the tip included?|Чаевые включены?
waiter|официант|Ask the waiter.|Спроси официанта.
order|заказ, заказывать|Are you ready to order?|Готовы сделать заказ?
rare / medium / well done|с кровью / средней прожарки / хорошо прожаренный|Medium, please.|Средней прожарки, пожалуйста.
refill|повторная порция напитка|Free refills.|Бесплатная доливка.
reservation|бронь|I have a reservation.|У меня забронировано.
Keep the change|Сдачи не надо|Here you are. Keep the change.|Вот, пожалуйста. Сдачи не надо.
Could I have some more…?|Можно ещё немного…?|Could I have some more bread?|Можно ещё хлеба?
It was delicious|Было очень вкусно|Thank you, it was delicious.|Спасибо, было очень вкусно.
`},
{
  id: 'en-home', emoji: '🏠', title: 'Дом и быт', level: 'A1', front: 'en-US', back: 'ru-RU',
  desc: 'Комнаты, мебель, бытовые дела.',
  cards: `
house|дом|They live in a big house.|Они живут в большом доме.
flat|квартира (брит.)|A two-room flat.|Двухкомнатная квартира.
apartment|квартира (амер.)|A small apartment downtown.|Небольшая квартира в центре.
room|комната|My room is upstairs.|Моя комната наверху.
kitchen|кухня|We eat in the kitchen.|Мы едим на кухне.
bedroom|спальня|The bedroom is cosy.|Спальня уютная.
bathroom|ванная|Where is the bathroom?|Где ванная?
living room|гостиная|Let's sit in the living room.|Посидим в гостиной.
hall|прихожая|Leave your shoes in the hall.|Оставь обувь в прихожей.
balcony|балкон|Tomatoes grow on our balcony.|На нашем балконе растут помидоры.
floor|пол, этаж|I live on the fifth floor.|Я живу на пятом этаже.
wall|стена|A picture on the wall.|Картина на стене.
ceiling|потолок|A high ceiling.|Высокий потолок.
window|окно|Open the window.|Открой окно.
door|дверь|Knock on the door.|Постучи в дверь.
stairs|лестница|Go up the stairs.|Поднимись по лестнице.
roof|крыша|A cat on the roof.|Кот на крыше.
table|стол|Set the table.|Накрой на стол.
chair|стул|Take a chair.|Возьми стул.
sofa|диван|Sleep on the sofa.|Спать на диване.
bed|кровать|Go to bed.|Иди спать.
wardrobe|шкаф для одежды|Hang it in the wardrobe.|Повесь это в шкаф.
shelf|полка|A book shelf.|Книжная полка.
mirror|зеркало|Look in the mirror.|Посмотри в зеркало.
lamp|лампа|Turn on the lamp.|Включи лампу.
carpet|ковёр|A red carpet.|Красный ковёр.
fridge|холодильник|Put it in the fridge.|Положи это в холодильник.
oven|духовка|Bake it in the oven.|Испеки это в духовке.
stove|плита|The pot is on the stove.|Кастрюля на плите.
sink|раковина|Dishes in the sink.|Посуда в раковине.
washing machine|стиральная машина|The washing machine is broken.|Стиральная машина сломалась.
key|ключ|Where are my keys?|Где мои ключи?
neighbour|сосед|Our neighbours are nice.|Наши соседи милые.
rent|арендная плата, снимать|We rent a flat.|Мы снимаем квартиру.
do the dishes|мыть посуду|It's your turn to do the dishes.|Твоя очередь мыть посуду.
do the laundry|стирать|I do the laundry on Sundays.|Я стираю по воскресеньям.
vacuum|пылесосить|Vacuum the carpet.|Пропылесось ковёр.
tidy up|прибираться|Tidy up your room.|Прибери в своей комнате.
take out the rubbish|выносить мусор|Can you take out the rubbish?|Можешь вынести мусор?
`},
{
  id: 'en-people', emoji: '👨‍👩‍👧', title: 'Семья и люди', level: 'A1', front: 'en-US', back: 'ru-RU',
  desc: 'Родственники, внешность, отношения.',
  cards: `
family|семья|A big family.|Большая семья.
parents|родители|My parents live in Kazan.|Мои родители живут в Казани.
mother|мать|My mother is a teacher.|Моя мама — учитель.
father|отец|Like father, like son.|Яблоко от яблони недалеко падает.
husband|муж|Her husband is a pilot.|Её муж — пилот.
wife|жена|My wife and I.|Мы с женой.
son|сын|They have a son.|У них есть сын.
daughter|дочь|Our daughter is five.|Нашей дочери пять.
brother|брат|My older brother.|Мой старший брат.
sister|сестра|My younger sister.|Моя младшая сестра.
grandmother|бабушка|Grandmother bakes pies.|Бабушка печёт пироги.
grandfather|дедушка|My grandfather was a sailor.|Мой дедушка был моряком.
uncle|дядя|My uncle lives abroad.|Мой дядя живёт за границей.
aunt|тётя|Aunt Mary.|Тётя Мэри.
cousin|двоюродный брат, сестра|My cousin is my age.|Мой двоюродный брат моего возраста.
nephew|племянник|My nephew is ten.|Моему племяннику десять.
niece|племянница|My niece loves dogs.|Моя племянница любит собак.
twins|близнецы|They are twins.|Они близнецы.
relatives|родственники|We visit our relatives.|Мы навещаем родственников.
friend|друг|A friend in need is a friend indeed.|Друг познаётся в беде.
boyfriend|парень (молодой человек)|Her boyfriend is tall.|Её парень высокий.
girlfriend|девушка (подруга)|His girlfriend is Spanish.|Его девушка испанка.
married|женатый, замужем|Are you married?|Вы женаты?
single|одинокий, не в браке|I'm single.|Я не в отношениях.
child|ребёнок|An only child.|Единственный ребёнок.
children|дети|Children love cartoons.|Дети любят мультфильмы.
baby|младенец|The baby is sleeping.|Малыш спит.
adult|взрослый|Tickets for adults.|Билеты для взрослых.
tall|высокий (о человеке)|He is tall.|Он высокий.
slim|стройный|She is slim.|Она стройная.
curly|кудрявый|Curly hair.|Кудрявые волосы.
handsome|красивый (о мужчине)|A handsome man.|Красивый мужчина.
pretty|симпатичная|A pretty girl.|Симпатичная девушка.
look like|быть похожим|You look like your mum.|Ты похожа на маму.
get on with|ладить с|I get on well with my sister.|Я хорошо лажу с сестрой.
grow up|вырастать|I grew up in a village.|Я вырос в деревне.
`},
{
  id: 'en-body', emoji: '🩺', title: 'Тело и здоровье', level: 'A2', front: 'en-US', back: 'ru-RU',
  desc: 'Части тела, симптомы, разговор с врачом.',
  cards: `
head|голова|My head hurts.|У меня болит голова.
face|лицо|Wash your face.|Умой лицо.
eye|глаз|She has blue eyes.|У неё голубые глаза.
ear|ухо|My ear hurts.|У меня болит ухо.
nose|нос|A runny nose.|Насморк.
mouth|рот|Open your mouth.|Откройте рот.
tooth|зуб|I have a toothache.|У меня болит зуб.
teeth|зубы|Brush your teeth.|Почисти зубы.
neck|шея|A long neck.|Длинная шея.
shoulder|плечо|My shoulder hurts.|У меня болит плечо.
arm|рука (от плеча до кисти)|I broke my arm.|Я сломал руку.
hand|кисть руки|Raise your hand.|Подними руку.
finger|палец|I cut my finger.|Я порезал палец.
back|спина|Back pain.|Боль в спине.
stomach|живот, желудок|My stomach hurts.|У меня болит живот.
leg|нога|Long legs.|Длинные ноги.
knee|колено|I hurt my knee.|Я ушиб колено.
foot|стопа|My foot is swollen.|У меня опухла стопа.
heart|сердце|A healthy heart.|Здоровое сердце.
skin|кожа|Dry skin.|Сухая кожа.
headache|головная боль|I have a terrible headache.|У меня ужасно болит голова.
fever|жар, температура|He has a fever.|У него температура.
cough|кашель|A bad cough.|Сильный кашель.
cold|простуда|I've caught a cold.|Я простудился.
sore throat|боль в горле|I have a sore throat.|У меня болит горло.
pain|боль|Where is the pain?|Где болит?
hurt|болеть, ранить|It hurts here.|Здесь болит.
ill|больной|I feel ill.|Мне плохо.
healthy|здоровый|Healthy food.|Здоровая еда.
medicine|лекарство|Take your medicine.|Прими лекарство.
pill|таблетка|Take one pill twice a day.|Принимайте по одной таблетке дважды в день.
pharmacy|аптека|Is there a pharmacy nearby?|Рядом есть аптека?
prescription|рецепт врача|You need a prescription.|Нужен рецепт.
appointment|запись (к врачу)|I'd like to make an appointment.|Я хотел бы записаться на приём.
ambulance|скорая помощь|Call an ambulance!|Вызовите скорую!
get better|выздоравливать|Get better soon!|Скорее поправляйся!
`},
{
  id: 'en-travel', emoji: '✈️', title: 'Путешествия', level: 'A2', front: 'en-US', back: 'ru-RU',
  desc: 'Аэропорт, отель, транспорт, дорога.',
  cards: `
trip|поездка|A business trip.|Командировка.
journey|путешествие, поездка|Have a nice journey!|Хорошей дороги!
ticket|билет|A one-way ticket.|Билет в одну сторону.
return ticket|билет туда-обратно|Two return tickets to London.|Два билета до Лондона туда и обратно.
passport|паспорт|Show your passport.|Покажите паспорт.
visa|виза|Do I need a visa?|Мне нужна виза?
luggage|багаж|Where can I collect my luggage?|Где получить багаж?
suitcase|чемодан|Pack your suitcase.|Собери чемодан.
backpack|рюкзак|A heavy backpack.|Тяжёлый рюкзак.
airport|аэропорт|Take me to the airport.|Отвезите меня в аэропорт.
flight|рейс, перелёт|My flight is delayed.|Мой рейс задерживается.
boarding pass|посадочный талон|Your boarding pass, please.|Ваш посадочный, пожалуйста.
gate|выход на посадку|Go to gate 12.|Пройдите к выходу 12.
departure|отправление, вылет|Departure time.|Время вылета.
arrival|прибытие|Arrivals hall.|Зал прилёта.
delay|задержка|A two-hour delay.|Задержка на два часа.
check in|регистрироваться, заселяться|Check in at the hotel.|Заселиться в отель.
check out|выезжать из отеля|Check out is at noon.|Выезд в полдень.
reception|ресепшн|Ask at reception.|Спросите на ресепшн.
single room|одноместный номер|A single room for two nights.|Одноместный номер на две ночи.
double room|двухместный номер|A double room with a view.|Двухместный номер с видом.
book|бронировать|I booked a hotel.|Я забронировал отель.
train|поезд|The train leaves at six.|Поезд отходит в шесть.
platform|платформа|Platform 9 ¾.|Платформа 9 ¾.
bus stop|автобусная остановка|Where is the bus stop?|Где автобусная остановка?
taxi|такси|Let's take a taxi.|Давай возьмём такси.
car|машина|I drive a car.|Я вожу машину.
map|карта|Show me on the map.|Покажите мне на карте.
abroad|за границей|I've never been abroad.|Я никогда не был за границей.
sightseeing|осмотр достопримечательностей|Let's go sightseeing.|Пойдём осматривать достопримечательности.
tourist|турист|Too many tourists.|Слишком много туристов.
souvenir|сувенир|A souvenir from Paris.|Сувенир из Парижа.
currency exchange|обмен валюты|Where is the currency exchange?|Где обмен валюты?
How far is it?|Как далеко это?|How far is it to the beach?|Далеко ли до пляжа?
I'm lost|Я заблудился|Excuse me, I'm lost.|Простите, я заблудился.
`},
{
  id: 'en-city', emoji: '🏙️', title: 'Город и направления', level: 'A1', front: 'en-US', back: 'ru-RU',
  desc: 'Места в городе и как спросить дорогу.',
  cards: `
street|улица|A busy street.|Оживлённая улица.
square|площадь|The main square.|Главная площадь.
bridge|мост|Cross the bridge.|Перейдите мост.
park|парк|A walk in the park.|Прогулка в парке.
bank|банк|The bank is closed.|Банк закрыт.
post office|почта|Where's the post office?|Где почта?
hospital|больница|He is in hospital.|Он в больнице.
school|школа|My son goes to school.|Мой сын ходит в школу.
library|библиотека|A quiet library.|Тихая библиотека.
museum|музей|The museum opens at ten.|Музей открывается в десять.
church|церковь|An old church.|Старая церковь.
shop|магазин|A small shop.|Маленький магазин.
supermarket|супермаркет|I'm going to the supermarket.|Я иду в супермаркет.
market|рынок|Fresh fruit at the market.|Свежие фрукты на рынке.
cinema|кинотеатр|Let's go to the cinema.|Пойдём в кино.
theatre|театр|A ticket to the theatre.|Билет в театр.
station|вокзал, станция|The train station.|Железнодорожный вокзал.
underground|метро (брит.)|Take the underground.|Поезжайте на метро.
crossroads|перекрёсток|Turn right at the crossroads.|На перекрёстке поверните направо.
traffic lights|светофор|Stop at the traffic lights.|Остановитесь на светофоре.
corner|угол|On the corner.|На углу.
turn left|поверните налево|Turn left after the bank.|После банка поверните налево.
turn right|поверните направо|Turn right here.|Здесь направо.
go straight on|идите прямо|Go straight on for 200 metres.|Идите прямо 200 метров.
next to|рядом с|Next to the café.|Рядом с кафе.
opposite|напротив|Opposite the station.|Напротив вокзала.
between|между|Between the bank and the shop.|Между банком и магазином.
behind|позади|Behind the church.|За церковью.
in front of|перед|In front of the hotel.|Перед отелем.
near|около, близко|Is it near here?|Это близко?
far|далеко|It's not far.|Это недалеко.
Excuse me, where is…?|Простите, где находится…?|Excuse me, where is the museum?|Простите, где музей?
`},
{
  id: 'en-work', emoji: '💼', title: 'Работа и офис', level: 'B1', front: 'en-US', back: 'ru-RU',
  desc: 'Должности, задачи, деловое общение.',
  cards: `
job|работа (должность)|I got a new job.|Я получил новую работу.
career|карьера|A career in IT.|Карьера в ИТ.
colleague|коллега|My colleagues are friendly.|Мои коллеги дружелюбные.
boss|начальник|My boss is strict.|Мой начальник строгий.
manager|менеджер, руководитель|Talk to the manager.|Поговорите с руководителем.
employee|сотрудник|The company has 50 employees.|В компании 50 сотрудников.
employer|работодатель|A good employer.|Хороший работодатель.
salary|зарплата|A high salary.|Высокая зарплата.
meeting|встреча, совещание|The meeting starts at 10.|Совещание начинается в 10.
deadline|крайний срок|We missed the deadline.|Мы пропустили дедлайн.
task|задача|I have many tasks today.|У меня сегодня много задач.
project|проект|A new project.|Новый проект.
report|отчёт|Write a report.|Напиши отчёт.
schedule|расписание, график|A busy schedule.|Плотный график.
interview|собеседование|A job interview.|Собеседование на работу.
resume|резюме|Send your resume.|Пришлите резюме.
experience|опыт|Work experience.|Опыт работы.
skill|навык|Communication skills.|Навыки общения.
hire|нанимать|They hired me.|Меня наняли.
fire|увольнять|He was fired.|Его уволили.
quit|уходить (с работы)|I quit my job.|Я уволился.
promotion|повышение|I got a promotion.|Меня повысили.
day off|выходной|Tomorrow is my day off.|Завтра у меня выходной.
holiday|отпуск, праздник|I'm on holiday.|Я в отпуске.
sick leave|больничный|She's on sick leave.|Она на больничном.
work from home|работать из дома|I work from home on Fridays.|По пятницам я работаю из дома.
agree|соглашаться|I agree with you.|Я с тобой согласен.
discuss|обсуждать|Let's discuss it.|Давай это обсудим.
solve|решать|Solve a problem.|Решить проблему.
improve|улучшать|Improve the results.|Улучшить результаты.
achieve|достигать|Achieve a goal.|Достичь цели.
responsible for|ответственный за|I'm responsible for sales.|Я отвечаю за продажи.
by the way|кстати|By the way, call Tom.|Кстати, позвони Тому.
as soon as possible|как можно скорее|Reply as soon as possible.|Ответьте как можно скорее.
Could you send me…?|Не могли бы вы прислать мне…?|Could you send me the file?|Не могли бы вы прислать мне файл?
I'm looking forward to…|С нетерпением жду…|I'm looking forward to your reply.|С нетерпением жду вашего ответа.
`},
{
  id: 'en-time', emoji: '⏰', title: 'Время и календарь', level: 'A1', front: 'en-US', back: 'ru-RU',
  desc: 'Дни, месяцы, частота и время суток.',
  cards: `
Monday|понедельник|See you on Monday.|Увидимся в понедельник.
Tuesday|вторник|Every Tuesday.|Каждый вторник.
Wednesday|среда|On Wednesday morning.|В среду утром.
Thursday|четверг|Thursday evening.|Вечер четверга.
Friday|пятница|Thank God it's Friday!|Слава богу, пятница!
Saturday|суббота|Saturday night.|Субботний вечер.
Sunday|воскресенье|A lazy Sunday.|Ленивое воскресенье.
January|январь|In January.|В январе.
February|февраль|February is short.|Февраль короткий.
March|март|Spring starts in March.|Весна начинается в марте.
April|апрель|April showers.|Апрельские дожди.
May|май|In May.|В мае.
June|июнь|Early June.|Начало июня.
July|июль|Hot July.|Жаркий июль.
August|август|Late August.|Конец августа.
September|сентябрь|School starts in September.|Школа начинается в сентябре.
October|октябрь|Golden October.|Золотой октябрь.
November|ноябрь|Rainy November.|Дождливый ноябрь.
December|декабрь|Snow in December.|Снег в декабре.
yesterday|вчера|I saw him yesterday.|Я видел его вчера.
today|сегодня|What day is it today?|Какой сегодня день?
tomorrow|завтра|See you tomorrow.|Увидимся завтра.
the day after tomorrow|послезавтра|We leave the day after tomorrow.|Мы уезжаем послезавтра.
week|неделя|Next week.|На следующей неделе.
month|месяц|Last month.|В прошлом месяце.
year|год|Happy New Year!|С Новым годом!
morning|утро|Good morning!|Доброе утро!
afternoon|день (после полудня)|In the afternoon.|Днём.
evening|вечер|Good evening!|Добрый вечер!
night|ночь|Good night!|Спокойной ночи!
always|всегда|I always drink coffee.|Я всегда пью кофе.
usually|обычно|I usually walk.|Я обычно хожу пешком.
often|часто|We often meet.|Мы часто встречаемся.
sometimes|иногда|Sometimes I cook.|Иногда я готовлю.
rarely|редко|I rarely watch TV.|Я редко смотрю телевизор.
never|никогда|Never give up!|Никогда не сдавайся!
half past|половина (о времени)|It's half past six.|Половина седьмого.
quarter to|без четверти|It's a quarter to nine.|Без четверти девять.
o'clock|ровно (о часах)|At five o'clock.|Ровно в пять.
`},
{
  id: 'en-nature', emoji: '🌦️', title: 'Природа и погода', level: 'A2', front: 'en-US', back: 'ru-RU',
  desc: 'Погода, времена года, животные и пейзажи.',
  cards: `
weather|погода|What's the weather like?|Какая погода?
sunny|солнечный|A sunny day.|Солнечный день.
cloudy|облачный|It's cloudy today.|Сегодня облачно.
rain|дождь|It's raining.|Идёт дождь.
snow|снег|Snow is falling.|Падает снег.
wind|ветер|A strong wind.|Сильный ветер.
storm|буря, гроза|A storm is coming.|Надвигается буря.
fog|туман|Thick fog.|Густой туман.
thunder|гром|Thunder and lightning.|Гром и молния.
rainbow|радуга|Look, a rainbow!|Смотри, радуга!
temperature|температура|The temperature is minus five.|Температура минус пять.
spring|весна|In spring.|Весной.
summer|лето|Summer holidays.|Летние каникулы.
autumn|осень|Autumn leaves.|Осенние листья.
winter|зима|A cold winter.|Холодная зима.
sky|небо|A blue sky.|Голубое небо.
sun|солнце|The sun is shining.|Солнце светит.
moon|луна|A full moon.|Полная луна.
star|звезда|Look at the stars.|Посмотри на звёзды.
sea|море|Swim in the sea.|Плавать в море.
river|река|A long river.|Длинная река.
lake|озеро|A quiet lake.|Тихое озеро.
mountain|гора|Climb a mountain.|Подняться на гору.
forest|лес|Walk in the forest.|Гулять по лесу.
field|поле|A field of flowers.|Поле цветов.
beach|пляж|Lie on the beach.|Лежать на пляже.
island|остров|A desert island.|Необитаемый остров.
tree|дерево|An old oak tree.|Старый дуб.
flower|цветок|Beautiful flowers.|Красивые цветы.
grass|трава|Green grass.|Зелёная трава.
dog|собака|My dog is friendly.|Моя собака дружелюбная.
cat|кошка|The cat is sleeping.|Кошка спит.
bird|птица|Birds are singing.|Птицы поют.
horse|лошадь|Ride a horse.|Ездить на лошади.
cow|корова|Cows give milk.|Коровы дают молоко.
bear|медведь|A brown bear.|Бурый медведь.
wolf|волк|A grey wolf.|Серый волк.
fox|лиса|A clever fox.|Хитрая лиса.
`},
{
  id: 'en-clothes', emoji: '👗', title: 'Одежда и покупки', level: 'A2', front: 'en-US', back: 'ru-RU',
  desc: 'Гардероб, размеры, примерка, оплата.',
  cards: `
clothes|одежда|Warm clothes.|Тёплая одежда.
shirt|рубашка|A white shirt.|Белая рубашка.
T-shirt|футболка|A cotton T-shirt.|Хлопковая футболка.
sweater|свитер|A wool sweater.|Шерстяной свитер.
jacket|куртка, пиджак|A leather jacket.|Кожаная куртка.
coat|пальто|A winter coat.|Зимнее пальто.
dress|платье|A red dress.|Красное платье.
skirt|юбка|A short skirt.|Короткая юбка.
trousers|брюки|Black trousers.|Чёрные брюки.
jeans|джинсы|Blue jeans.|Синие джинсы.
shoes|туфли, обувь|New shoes.|Новые туфли.
boots|сапоги, ботинки|Winter boots.|Зимние ботинки.
trainers|кроссовки|Running trainers.|Беговые кроссовки.
socks|носки|A pair of socks.|Пара носков.
hat|шляпа, шапка|Put on your hat.|Надень шапку.
scarf|шарф|A warm scarf.|Тёплый шарф.
gloves|перчатки|Leather gloves.|Кожаные перчатки.
belt|ремень|A brown belt.|Коричневый ремень.
bag|сумка|A shopping bag.|Сумка для покупок.
size|размер|What size are you?|Какой у вас размер?
try on|примерять|Can I try it on?|Можно примерить?
fitting room|примерочная|Where's the fitting room?|Где примерочная?
fit|подходить по размеру|It fits perfectly.|Сидит идеально.
suit|идти (о стиле, цвете)|Red suits you.|Тебе идёт красный.
price|цена|What's the price?|Какая цена?
How much is it?|Сколько это стоит?|How much is this jacket?|Сколько стоит эта куртка?
discount|скидка|Is there a discount?|Есть скидка?
sale|распродажа|Everything is on sale.|Всё по распродаже.
receipt|чек|Keep the receipt.|Сохраните чек.
cash|наличные|I'll pay in cash.|Я заплачу наличными.
change|сдача|Here's your change.|Вот ваша сдача.
refund|возврат денег|Can I get a refund?|Можно вернуть деньги?
wear|носить (одежду)|She always wears black.|Она всегда носит чёрное.
put on|надевать|Put on your coat.|Надень пальто.
take off|снимать|Take off your shoes.|Сними обувь.
`},
{
  id: 'en-feelings', emoji: '💛', title: 'Эмоции и характер', level: 'B1', front: 'en-US', back: 'ru-RU',
  desc: 'Как описать чувства и людей.',
  cards: `
angry|злой, сердитый|Don't be angry with me.|Не злись на меня.
afraid|испуганный|I'm afraid of spiders.|Я боюсь пауков.
scared|напуганный|The kids were scared.|Дети испугались.
worried|обеспокоенный|I'm worried about you.|Я волнуюсь за тебя.
nervous|нервничающий|I'm nervous before exams.|Я нервничаю перед экзаменами.
excited|взволнованный (радостно)|I'm so excited!|Я так взволнован!
surprised|удивлённый|I was surprised to see her.|Я удивился, увидев её.
disappointed|разочарованный|I'm disappointed in you.|Я в тебе разочарован.
embarrassed|смущённый|I felt embarrassed.|Мне было неловко.
proud|гордый|I'm proud of you.|Я горжусь тобой.
jealous|ревнивый, завистливый|He is jealous.|Он ревнует.
lonely|одинокий|I feel lonely.|Мне одиноко.
relaxed|расслабленный|I feel relaxed.|Я расслаблен.
grateful|благодарный|I'm grateful for your help.|Я благодарен за помощь.
confused|сбитый с толку|I'm confused.|Я запутался.
bored|скучающий|I'm bored.|Мне скучно.
kind|добрый|You are very kind.|Вы очень добры.
honest|честный|An honest answer.|Честный ответ.
lazy|ленивый|Don't be lazy!|Не ленись!
hard-working|трудолюбивый|She is hard-working.|Она трудолюбивая.
shy|застенчивый|He is shy.|Он стеснительный.
brave|смелый|Be brave.|Будь смелым.
clever|умный|A clever idea.|Умная идея.
funny|смешной, забавный|A funny story.|Смешная история.
friendly|дружелюбный|Friendly people.|Дружелюбные люди.
polite|вежливый|Be polite.|Будь вежлив.
rude|грубый|That's rude.|Это грубо.
patient|терпеливый|Be patient.|Будь терпелив.
generous|щедрый|A generous gift.|Щедрый подарок.
selfish|эгоистичный|Don't be selfish.|Не будь эгоистом.
reliable|надёжный|A reliable friend.|Надёжный друг.
confident|уверенный в себе|She looks confident.|Она выглядит уверенно.
calm down|успокоиться|Calm down, please.|Успокойся, пожалуйста.
cheer up|взбодриться, не унывать|Cheer up!|Не грусти!
`},
{
  id: 'en-phrases', emoji: '💬', title: 'Фразы для общения', level: 'A1', front: 'en-US', back: 'ru-RU',
  desc: 'Готовые выражения на каждый день — учите целиком.',
  cards: `
How are you?|Как дела?|— How are you? — Fine, thanks.|— Как дела? — Хорошо, спасибо.
What's up?|Как жизнь? Что нового?|Hey, what's up?|Привет, как жизнь?
Nice to meet you|Приятно познакомиться|Hi, I'm Anna. Nice to meet you.|Привет, я Анна. Приятно познакомиться.
See you later|До встречи|Bye! See you later.|Пока! До встречи.
Take care|Береги себя|Goodbye, take care!|До свидания, береги себя!
Thank you very much|Большое спасибо|Thank you very much for your help.|Большое спасибо за помощь.
You're welcome|Пожалуйста (в ответ на спасибо)|— Thanks! — You're welcome.|— Спасибо! — Пожалуйста.
Excuse me|Извините (привлечь внимание)|Excuse me, is this seat free?|Извините, это место свободно?
I'm sorry|Мне жаль, простите|I'm sorry I'm late.|Простите, что опоздал.
No problem|Без проблем|— Sorry! — No problem.|— Извини! — Без проблем.
Could you repeat that?|Не могли бы вы повторить?|Sorry, could you repeat that?|Простите, не могли бы вы повторить?
Could you speak more slowly?|Говорите медленнее, пожалуйста|Could you speak more slowly, please?|Не могли бы вы говорить медленнее?
What does … mean?|Что значит …?|What does "awesome" mean?|Что значит «awesome»?
How do you say … in English?|Как сказать … по-английски?|How do you say «вилка» in English?|Как по-английски «вилка»?
I don't know|Я не знаю|Sorry, I don't know.|Извините, я не знаю.
I see|Понятно|Oh, I see.|А, понятно.
Of course|Конечно|Of course, come in!|Конечно, заходи!
Never mind|Ничего страшного, неважно|Never mind, it's not important.|Неважно, это не главное.
Good luck!|Удачи!|Good luck with your exam!|Удачи на экзамене!
Congratulations!|Поздравляю!|Congratulations on your new job!|Поздравляю с новой работой!
Help yourself|Угощайтесь|Help yourself to some cake.|Угощайтесь тортом.
Make yourself at home|Чувствуйте себя как дома|Come in, make yourself at home.|Заходите, чувствуйте себя как дома.
It's up to you|Решать тебе|Pizza or sushi? It's up to you.|Пицца или суши? Решать тебе.
I'm not sure|Я не уверен|I'm not sure it's a good idea.|Не уверен, что это хорошая идея.
That sounds great|Звучит отлично|Dinner at eight? That sounds great!|Ужин в восемь? Звучит отлично!
Don't worry|Не волнуйся|Don't worry, everything will be fine.|Не волнуйся, всё будет хорошо.
What do you think?|Что ты думаешь?|What do you think about it?|Что ты об этом думаешь?
Let me think|Дай подумать|Hmm, let me think.|Хм, дай подумать.
I agree|Я согласен|I agree with you completely.|Я полностью с тобой согласен.
Have a nice day!|Хорошего дня!|Thanks, have a nice day!|Спасибо, хорошего дня!
`},
{
  id: 'en-irregular', emoji: '🔁', title: 'Неправильные глаголы', level: 'A2', front: 'en-US', back: 'ru-RU',
  desc: 'Три формы самых частых неправильных глаголов. Проговаривайте вслух ритмом.',
  cards: `
be — was/were — been|быть|I have been to London.|Я бывал в Лондоне.
begin — began — begun|начинать|The film has begun.|Фильм начался.
break — broke — broken|ломать|I broke my phone.|Я разбил телефон.
bring — brought — brought|приносить|She brought a cake.|Она принесла торт.
build — built — built|строить|They built a house.|Они построили дом.
buy — bought — bought|покупать|I bought a new car.|Я купил новую машину.
catch — caught — caught|ловить|He caught a fish.|Он поймал рыбу.
choose — chose — chosen|выбирать|I chose the red one.|Я выбрал красный.
come — came — come|приходить|He came late.|Он пришёл поздно.
cost — cost — cost|стоить|It cost ten dollars.|Это стоило десять долларов.
do — did — done|делать|I did my homework.|Я сделал домашку.
drink — drank — drunk|пить|I drank some water.|Я выпил воды.
drive — drove — driven|водить машину|She drove to work.|Она поехала на работу на машине.
eat — ate — eaten|есть|We ate pizza.|Мы ели пиццу.
fall — fell — fallen|падать|He fell down.|Он упал.
feel — felt — felt|чувствовать|I felt better.|Мне стало лучше.
find — found — found|находить|I found my keys.|Я нашёл ключи.
fly — flew — flown|летать|We flew to Paris.|Мы полетели в Париж.
forget — forgot — forgotten|забывать|I forgot his name.|Я забыл его имя.
get — got — got|получать|I got your message.|Я получил твоё сообщение.
give — gave — given|давать|He gave me a gift.|Он подарил мне подарок.
go — went — gone|идти|They went home.|Они пошли домой.
have — had — had|иметь|We had a great time.|Мы отлично провели время.
hear — heard — heard|слышать|I heard a noise.|Я услышал шум.
keep — kept — kept|хранить|She kept the secret.|Она сохранила секрет.
know — knew — known|знать|I knew it!|Я так и знал!
leave — left — left|уходить, оставлять|He left early.|Он ушёл рано.
lose — lost — lost|терять|We lost the game.|Мы проиграли игру.
make — made — made|делать, создавать|Mum made dinner.|Мама приготовила ужин.
meet — met — met|встречать|We met in 2015.|Мы познакомились в 2015-м.
pay — paid — paid|платить|I paid the bill.|Я оплатил счёт.
put — put — put|класть|I put it on the table.|Я положил это на стол.
read — read — read|читать|I read that book.|Я читал ту книгу.
run — ran — run|бежать|She ran fast.|Она бежала быстро.
say — said — said|сказать|He said yes.|Он сказал «да».
see — saw — seen|видеть|I saw a film.|Я посмотрел фильм.
sell — sold — sold|продавать|They sold the house.|Они продали дом.
send — sent — sent|отправлять|I sent an email.|Я отправил письмо.
sing — sang — sung|петь|She sang a song.|Она спела песню.
sit — sat — sat|сидеть|We sat by the fire.|Мы сидели у огня.
sleep — slept — slept|спать|I slept well.|Я хорошо спал.
speak — spoke — spoken|говорить|We spoke English.|Мы говорили по-английски.
spend — spent — spent|тратить|I spent all my money.|Я потратил все деньги.
swim — swam — swum|плавать|We swam in the lake.|Мы плавали в озере.
take — took — taken|брать|He took my pen.|Он взял мою ручку.
teach — taught — taught|учить (кого-то)|She taught me Spanish.|Она учила меня испанскому.
tell — told — told|рассказывать|He told a joke.|Он рассказал анекдот.
think — thought — thought|думать|I thought so.|Я так и думал.
understand — understood — understood|понимать|I understood everything.|Я всё понял.
wear — wore — worn|носить|She wore a hat.|Она была в шляпе.
win — won — won|побеждать|We won!|Мы победили!
write — wrote — written|писать|He wrote a letter.|Он написал письмо.
`},
{
  id: 'en-phrasal', emoji: '🧩', title: 'Фразовые глаголы', level: 'B1', front: 'en-US', back: 'ru-RU',
  desc: 'Самые частые фразовые глаголы разговорной речи.',
  cards: `
get up|вставать|I get up at seven.|Я встаю в семь.
wake up|просыпаться|Wake up! It's late.|Просыпайся! Уже поздно.
give up|бросать, сдаваться|Don't give up!|Не сдавайся!
look for|искать|I'm looking for my glasses.|Я ищу очки.
look after|присматривать за|Can you look after my cat?|Присмотришь за моей кошкой?
look forward to|ждать с нетерпением|I look forward to seeing you.|Жду встречи с тобой.
find out|узнавать, выяснять|Find out the price.|Узнай цену.
turn on|включать|Turn on the light.|Включи свет.
turn off|выключать|Turn off your phone.|Выключи телефон.
turn down|отклонять, убавлять|She turned down the offer.|Она отклонила предложение.
pick up|поднимать, забирать|I'll pick you up at six.|Я заберу тебя в шесть.
put off|откладывать|Don't put it off till tomorrow.|Не откладывай это на завтра.
carry on|продолжать|Carry on, please.|Продолжайте, пожалуйста.
come back|возвращаться|Come back soon!|Возвращайся скорее!
go out|выходить, гулять|Let's go out tonight.|Пойдём куда-нибудь вечером.
set up|устраивать, настраивать|Set up a meeting.|Назначь встречу.
run out of|заканчиваться (о запасе)|We've run out of milk.|У нас закончилось молоко.
break down|ломаться|My car broke down.|Моя машина сломалась.
check in|регистрироваться|Check in online.|Зарегистрируйся онлайн.
fill in|заполнять (бланк)|Fill in this form.|Заполните эту форму.
make up|выдумывать; мириться|They argued and made up.|Они поссорились и помирились.
work out|тренироваться; получаться|It worked out well.|Всё получилось хорошо.
figure out|разобраться, понять|I can't figure it out.|Не могу в этом разобраться.
put on|надевать|Put on a jacket.|Надень куртку.
take off|снимать; взлетать|The plane took off.|Самолёт взлетел.
get on|садиться (в транспорт); ладить|Get on the bus.|Садись в автобус.
get off|выходить (из транспорта)|Get off at the next stop.|Выйди на следующей остановке.
hang out|тусоваться, проводить время|We hang out on weekends.|Мы проводим время вместе по выходным.
show up|появляться|He didn't show up.|Он не пришёл.
call back|перезванивать|I'll call you back.|Я тебе перезвоню.
come up with|придумывать|She came up with a plan.|Она придумала план.
get rid of|избавляться|Get rid of old clothes.|Избавься от старой одежды.
`},
{
  id: 'en-links', emoji: '🔗', title: 'Связки и предлоги', level: 'B1', front: 'en-US', back: 'ru-RU',
  desc: 'Слова-связки делают речь плавной и логичной.',
  cards: `
and|и|Tea and coffee.|Чай и кофе.
but|но|I like it, but it's expensive.|Мне нравится, но это дорого.
or|или|Tea or coffee?|Чай или кофе?
because|потому что|I stayed home because it rained.|Я остался дома, потому что шёл дождь.
so|поэтому, так что|I was tired, so I went to bed.|Я устал, поэтому лёг спать.
although|хотя|Although it was cold, we swam.|Хотя было холодно, мы плавали.
however|однако|However, there is a problem.|Однако есть проблема.
therefore|следовательно|Therefore, we must act now.|Следовательно, нужно действовать сейчас.
moreover|более того|Moreover, it's cheap.|Более того, это дёшево.
instead|вместо этого|Let's walk instead.|Давай лучше пройдёмся.
unless|если не|I'll come unless it rains.|Приду, если не будет дождя.
until|до тех пор пока|Wait until I come.|Жди, пока я не приду.
while|пока, в то время как|I read while she cooked.|Я читал, пока она готовила.
since|с (какого-то времени); так как|I've lived here since 2010.|Я живу здесь с 2010 года.
during|во время|During the film.|Во время фильма.
before|до, перед|Before dinner.|Перед ужином.
after|после|After work.|После работы.
then|затем, тогда|First eat, then play.|Сначала поешь, потом играй.
finally|наконец|Finally, we arrived.|Наконец мы приехали.
for example|например|For example, apples.|Например, яблоки.
in fact|на самом деле|In fact, I agree.|На самом деле я согласен.
anyway|в любом случае|Anyway, let's go.|В любом случае, пошли.
actually|вообще-то, фактически|Actually, I'm busy.|Вообще-то я занят.
of course|конечно|Of course I'll help.|Конечно, я помогу.
at least|по крайней мере|At least try it.|Хотя бы попробуй.
on the other hand|с другой стороны|On the other hand, it's fast.|С другой стороны, это быстро.
in my opinion|по моему мнению|In my opinion, it's wrong.|По-моему, это неправильно.
as well|тоже, также|I want some as well.|Я тоже хочу.
either … or|либо … либо|Either now or never.|Сейчас или никогда.
neither … nor|ни … ни|Neither he nor she knows.|Ни он, ни она не знают.
`},
{
  id: 'en-tech', emoji: '💻', title: 'Технологии и интернет', level: 'B1', front: 'en-US', back: 'ru-RU',
  desc: 'Гаджеты, приложения и всё цифровое.',
  cards: `
device|устройство|A mobile device.|Мобильное устройство.
screen|экран|A cracked screen.|Треснувший экран.
keyboard|клавиатура|A wireless keyboard.|Беспроводная клавиатура.
charger|зарядка|I forgot my charger.|Я забыл зарядку.
battery|батарея, аккумулятор|My battery is low.|У меня садится батарея.
password|пароль|Forgot your password?|Забыли пароль?
account|аккаунт, счёт|Create an account.|Создайте аккаунт.
download|скачивать|Download the app.|Скачайте приложение.
upload|загружать (на сервер)|Upload a photo.|Загрузите фото.
update|обновление, обновлять|Update your phone.|Обнови телефон.
install|устанавливать|Install the program.|Установите программу.
search|поиск, искать|Search the web.|Поищи в интернете.
link|ссылка|Send me the link.|Пришли мне ссылку.
file|файл|Save the file.|Сохрани файл.
folder|папка|Open the folder.|Открой папку.
settings|настройки|Go to settings.|Зайди в настройки.
notification|уведомление|Turn off notifications.|Отключи уведомления.
message|сообщение|Text me a message.|Напиши мне сообщение.
attach|прикреплять|Attach the document.|Прикрепите документ.
delete|удалять|Delete this photo.|Удали это фото.
save|сохранять|Save your work.|Сохрани работу.
share|делиться|Share the post.|Поделись постом.
connect|подключать|Connect to Wi-Fi.|Подключись к Wi-Fi.
network|сеть|A social network.|Социальная сеть.
browser|браузер|Open your browser.|Откройте браузер.
website|сайт|Visit our website.|Посетите наш сайт.
online|онлайн, в сети|Are you online?|Ты в сети?
offline|не в сети|Works offline.|Работает без интернета.
log in|войти в систему|Log in to your account.|Войдите в свой аккаунт.
log out|выйти из системы|Don't forget to log out.|Не забудьте выйти.
`},
{
  id: 'en-hobby', emoji: '⚽', title: 'Спорт и хобби', level: 'A2', front: 'en-US', back: 'ru-RU',
  desc: 'Чем заняться в свободное время.',
  cards: `
hobby|хобби|What's your hobby?|Какое у тебя хобби?
free time|свободное время|In my free time I read.|В свободное время я читаю.
football|футбол|Play football.|Играть в футбол.
swimming|плавание|I love swimming.|Я обожаю плавать.
running|бег|Running is good for health.|Бег полезен для здоровья.
cycling|езда на велосипеде|Cycling to work.|На работу на велосипеде.
skiing|катание на лыжах|Skiing in the mountains.|Катание на лыжах в горах.
yoga|йога|Do yoga.|Заниматься йогой.
gym|спортзал|I go to the gym.|Я хожу в спортзал.
team|команда|Our team won.|Наша команда победила.
match|матч|A football match.|Футбольный матч.
score|счёт; забивать|What's the score?|Какой счёт?
coach|тренер|Our coach is strict.|Наш тренер строгий.
painting|живопись|Painting relaxes me.|Рисование меня расслабляет.
drawing|рисование (карандашом)|A pencil drawing.|Рисунок карандашом.
photography|фотография|I'm into photography.|Я увлекаюсь фотографией.
cooking|готовка|Cooking is fun.|Готовить — весело.
gardening|садоводство|Gardening in spring.|Садоводство весной.
fishing|рыбалка|Go fishing.|Пойти на рыбалку.
hiking|пеший туризм|Hiking in the hills.|Походы по холмам.
dancing|танцы|Dancing all night.|Танцы всю ночь.
board games|настольные игры|We play board games.|Мы играем в настольные игры.
knitting|вязание|Knitting a scarf.|Вязание шарфа.
musical instrument|музыкальный инструмент|Do you play a musical instrument?|Ты играешь на каком-нибудь инструменте?
guitar|гитара|Play the guitar.|Играть на гитаре.
be into|увлекаться|I'm into jazz.|Я увлекаюсь джазом.
be good at|хорошо уметь|She's good at chess.|Она хорошо играет в шахматы.
take up|начать заниматься|I took up tennis.|Я начал заниматься теннисом.
`},
{
  id: 'en-money', emoji: '💳', title: 'Деньги и банк', level: 'B1', front: 'en-US', back: 'ru-RU',
  desc: 'Финансы в быту: оплата, счета, накопления.',
  cards: `
money|деньги|Time is money.|Время — деньги.
coin|монета|A gold coin.|Золотая монета.
banknote|банкнота|A 100-euro banknote.|Купюра в 100 евро.
wallet|кошелёк|I left my wallet at home.|Я оставил кошелёк дома.
credit card|кредитная карта|Pay by credit card.|Оплатить кредиткой.
cash machine|банкомат|Is there a cash machine here?|Здесь есть банкомат?
withdraw|снимать (деньги)|Withdraw cash.|Снять наличные.
transfer|перевод, переводить|A bank transfer.|Банковский перевод.
loan|заём, кредит|Take out a loan.|Взять кредит.
debt|долг|I'm in debt.|Я в долгах.
borrow|брать в долг|Can I borrow ten dollars?|Можно занять десять долларов?
lend|давать в долг|Lend me your pen.|Одолжи мне ручку.
save|копить|Save money for a trip.|Копить на поездку.
savings|сбережения|All my savings.|Все мои сбережения.
spend|тратить|Don't spend too much.|Не трать слишком много.
afford|позволить себе|I can't afford it.|Я не могу себе это позволить.
budget|бюджет|A tight budget.|Скромный бюджет.
bill|счёт к оплате|Pay the bills.|Оплатить счета.
tax|налог|Income tax.|Подоходный налог.
income|доход|A monthly income.|Ежемесячный доход.
earn|зарабатывать|How much do you earn?|Сколько ты зарабатываешь?
worth|стоящий|It's worth the money.|Оно того стоит.
fee|плата, сбор|An entrance fee.|Плата за вход.
`},
{
  id: 'en-idioms', emoji: '🎭', title: 'Идиомы', level: 'B2', front: 'en-US', back: 'ru-RU',
  desc: 'Популярные идиомы — для живой, естественной речи.',
  cards: `
a piece of cake|проще простого|The test was a piece of cake.|Тест был проще простого.
break the ice|растопить лёд, начать общение|A joke helps to break the ice.|Шутка помогает растопить лёд.
hit the books|засесть за учёбу|I need to hit the books.|Мне нужно засесть за учебники.
under the weather|неважно себя чувствовать|I'm a bit under the weather.|Мне немного нездоровится.
once in a blue moon|очень редко|I see him once in a blue moon.|Я вижу его крайне редко.
cost an arm and a leg|стоить целое состояние|This car costs an arm and a leg.|Эта машина стоит целое состояние.
let the cat out of the bag|проболтаться|Who let the cat out of the bag?|Кто проболтался?
it's raining cats and dogs|льёт как из ведра|Take an umbrella, it's raining cats and dogs.|Возьми зонт, льёт как из ведра.
kill two birds with one stone|убить двух зайцев|Let's kill two birds with one stone.|Давай убьём двух зайцев.
better late than never|лучше поздно, чем никогда|You came! Better late than never.|Ты пришёл! Лучше поздно, чем никогда.
the ball is in your court|теперь твой ход|I've done my part, the ball is in your court.|Я своё сделал, теперь твой ход.
call it a day|закончить на сегодня|Let's call it a day.|Давай на сегодня закончим.
get cold feet|струсить в последний момент|He got cold feet before the wedding.|Он струсил перед свадьбой.
in hot water|в беде, в неприятностях|He's in hot water with his boss.|У него неприятности с начальником.
on cloud nine|на седьмом небе|She's on cloud nine.|Она на седьмом небе от счастья.
pull someone's leg|разыгрывать кого-то|Are you pulling my leg?|Ты меня разыгрываешь?
see eye to eye|сходиться во мнениях|We don't see eye to eye.|Мы не сходимся во мнениях.
spill the beans|выдать секрет|Come on, spill the beans!|Ну же, выкладывай!
a blessing in disguise|не было бы счастья, да несчастье помогло|Losing that job was a blessing in disguise.|Потеря той работы оказалась к лучшему.
bite off more than you can chew|взвалить на себя слишком много|Don't bite off more than you can chew.|Не бери на себя слишком много.
keep an eye on|присматривать за|Keep an eye on the kids.|Присмотри за детьми.
no pain, no gain|без труда не вытащишь и рыбку из пруда|Keep training — no pain, no gain.|Тренируйся — без труда ничего не добьёшься.
`}
];

/* Короткие советы — показываются на главном экране по одному в день. */
window.MNEMO_TIPS = [
  'Учите понемногу, но каждый день: 15 минут ежедневно лучше, чем 2 часа раз в неделю.',
  'Произносите слово вслух — так подключается слуховая и моторная память.',
  'Придумайте к трудному слову нелепую картинку-ассоциацию и запишите её в поле «Подсказка».',
  'Сначала вспомните ответ сами и только потом переворачивайте карточку — это главное в запоминании.',
  'Ошибки — это хорошо: слово, которое вы вспомнили с трудом, запомнится крепче.',
  'Учите слова в примерах: «make a decision», а не просто «decision».',
  'Перемешивайте темы: чередование наборов тренирует мозг различать похожие слова.',
  'Повторяйте перед сном: во сне мозг закрепляет выученное за день.',
  'Используйте новые слова в своей речи в тот же день — хотя бы в мыслях.',
  'Не учите больше 10–15 новых слов за раз — лучше закрепите их уроком и повторением.',
  'Пишите ответы руками в режиме «Письмо» — набор текста запоминается лучше, чем узнавание.',
  'Слушайте слово в «Диктанте», не глядя на текст, — так тренируется восприятие на слух.',
  'Связывайте слово с собой: «Я вчера forgot ключи» — личный пример запоминается сильнее.',
  'Возвращайтесь к повторению, когда приложение говорит, — интервалы рассчитаны под кривую забывания.',
];
