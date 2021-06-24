import React from 'react';
import { makeStyles } from '@material-ui/core/styles';
import Card from '@material-ui/core/Card';
import CardContent from '@material-ui/core/CardContent';
import Typography from '@material-ui/core/Typography';

const useStyles = makeStyles({
  root: {
    minWidth: 275,
  },
  bullet: {
    display: 'inline-block',
    margin: '0 2px',
    transform: 'scale(0.8)',
  },
  title: {
    fontSize: 14,
  },
  pos: {
    marginBottom: 16,
  },
});

export default function OutlinedCard(props) {
  const classes = useStyles();
  const bull = <span className={classes.bullet}>•</span>;

  return (
    <Card className={classes.root} variant="outlined">
      <CardContent>
      <Typography className={classes.pos} color="textSecondary">
          {props.date.toString()}
        </Typography>
        <Typography className={classes.title} color="textSecondary" gutterBottom>
          Author
        </Typography>
        <Typography className={classes.pos} variant="h5" component="h2">
        {props.data.author.name}{bull}{props.data.author.email}
          {/* <a href={"https://github.com/" + props.data.author.name}>{props.data.author.name}{bull}{props.data.author.email}</a> */}
        </Typography>
        <Typography className={classes.title} color="textSecondary">
          Message
        </Typography>
        <Typography className={classes.pos} variant="body2" component="p">
          {props.data.message}
        </Typography>

        {
          props.data.clossesIssue && <div>
            <Typography className={classes.title} color="textSecondary">
          Closses Issue
        </Typography>
        <Typography className={classes.pos} variant="body2" component="p">
        <a href={"https://github.com/" + props.projectName + "/issues/" + props.data.clossesIssue} target="_blank" rel="noopener noreferrer">#{props.data.clossesIssue}</a>
          {/* {props.data.clossesIssue} */}
        </Typography>
        </div>
        }
        <Typography className={classes.pos}  color="textSecondary">
          Body
        </Typography>
        <Typography  className={classes.pos}variant="body2" component="p">
          {props.data.body}
        </Typography>
        <Typography className={classes.title} color="textSecondary">
          Note
        </Typography>
        <Typography variant="body2" component="p">
          {props.data.note}
        </Typography>
      </CardContent>
      {/* <CardActions>
        <Button size="small">Learn More</Button>
      </CardActions> */}
    </Card>
  );
}
