import 'package:flutter/material.dart';
import 'eventdata.dart';

class EventCard extends StatefulWidget {
  final EventData event;

  EventCard(this.event);

  @override
  State<StatefulWidget> createState() {
    return EventCardState(event);
  }
}

class EventCardState extends State<EventCard> {
  EventData baby;
  String renderUrl;

  EventCardState(this.baby);

  Widget get babyCard {
    return
      new Card(
          child: Column(
              mainAxisSize: MainAxisSize.min,
              children: <Widget>[
                ListTile(
                  leading: const Icon(Icons.album),
                  title: Text('The ${baby.name} is having:'),
                  subtitle: Text('${baby.vote} Votes.'),
                ),
                new ButtonTheme.bar( // make buttons use the appropriate styles for cards
                    child: new ButtonBar(
                        children: <Widget>[
                          new FlatButton(
                            child: const Text('Thumb up'),
                            onPressed: () { /* ... */ },
                          ),
                          new FlatButton(
                            child: const Text('Thumb down'),
                            onPressed: () { /* ... */ },
                          )]))]));
  }

  @override
  Widget build(BuildContext context) {
    return new Container(
      child:  babyCard,
    );
  }
}