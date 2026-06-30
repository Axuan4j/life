import { createApp } from 'vue';
import { createPinia } from 'pinia';
import {
  ActionBar,
  ActionBarButton,
  Badge,
  Button,
  Cell,
  CellGroup,
  ConfigProvider,
  Empty,
  Field,
  Form,
  Icon,
  List,
  NavBar,
  Popup,
  PullRefresh,
  Search,
  Swipe,
  SwipeItem,
  Sticky,
  Tab,
  Tabbar,
  TabbarItem,
  Tag,
  Tabs,
  Toast,
} from 'vant';
import 'vant/lib/index.css';
import './styles/tokens.css';
import App from './App.vue';
import router from './router';

const app = createApp(App);
app.use(createPinia());
app.use(router);
app.use(ConfigProvider);
app.use(Badge);
app.use(Button);
app.use(Cell);
app.use(CellGroup);
app.use(Field);
app.use(Form);
app.use(Empty);
app.use(Icon);
app.use(List);
app.use(NavBar);
app.use(Popup);
app.use(PullRefresh);
app.use(Search);
app.use(Swipe);
app.use(SwipeItem);
app.use(Sticky);
app.use(Tab);
app.use(Tabbar);
app.use(TabbarItem);
app.use(Tag);
app.use(Tabs);
app.use(ActionBar);
app.use(ActionBarButton);
app.use(Toast);
app.mount('#app');
